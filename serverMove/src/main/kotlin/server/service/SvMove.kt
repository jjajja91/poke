package server.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.batch.SvBatch
import server.entity.EntMove
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import server.repository.RepoMove
import scan.util.coroutine.AppScope
import scan.util.coroutine.JobStatusStore
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonConst
import server.dto.PokemonMoveDTO
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrNull

@Service
class SvMove(
    private val moveRepository: RepoMove,
    private val mapper: ObjectMapper,
    private val pokemonWebClient: WebClient,
    private val svBatch: SvBatch,
    private val appScope: AppScope,
    private val statusStore: JobStatusStore
) {
    private val DOMAIN = EnumFailDomain.MOVE
    fun getName(name: String): EntMove {
        val move = moveRepository.findByNameEnIgnoreCase(name) ?: throw Throwable("Move $name not found")
        return move
    }
    // force 저장 진입점 (요청만 하고 바로 응답 / 백그라운드에서 진행)
    fun startAddAllForce():String {
        val jobId = UUID.randomUUID().toString()
        statusStore.start(jobId, name = "${DOMAIN.name}:addAllForce")
        appScope.launchApp("${DOMAIN.name}:addAllForce$jobId") {
            runAddAllForce(jobId)
        }
        return jobId
    }
    private suspend fun runAddAllForce(jobId: String) {
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        try {
            // 1) 기존 데이터/실패 기록 정리 (DB 작업이므로 IO)
            withContext(Dispatchers.IO) {
                moveRepository.deleteAllInBatch()
                svBatch.deleteAllFail(DOMAIN)
            }

            // 2) 전체 idSet 가져오기 (네트워크 suspend)
            val idSet = PokemonConst.getIdSet(pokemonWebClient, DOMAIN.apiKey)
            statusStore.update(jobId, total = idSet.size)
            if (idSet.isEmpty()) {
                statusStore.done(jobId, success = 0, fail = 0)
                return
            }
            // 3) 병렬 수집
            val result = idSet.retryAwaitAll(
                retry = 3, concurrency = 7, delayMs = 3000
            ) { id ->
                item(id)
            }
            // 간단 집계
            successCount.set(result.successList.size)
            failCount.set(result.failList.size)
            // 4) DB 저장
            withContext(Dispatchers.IO) {
                svBatch.batchAll(DOMAIN, result) { sl ->
                    val pureList = sl.filter { it.typeId in PokemonConst.TYPE_ID_SET }
                    moveRepository.saveAll(pureList)
                }
            }
            statusStore.done(jobId, success = successCount.get(), fail = failCount.get())
        } catch (t: Throwable) {
            statusStore.failed(jobId, success = successCount.get(), fail = failCount.get(), message = t.message)
            throw t
        }
    }
    suspend fun addAllCheck() {
        val failList = svBatch.findAllFail(DOMAIN)
        addList(failList.map { it.refId }.toSet())
    }
    private suspend fun addList(idSet:Set<Int>) {
        if (idSet.isEmpty()) return
        val result = idSet.retryAwaitAll(
            retry = 3, concurrency = 7, delayMs = 3000
        ) { id ->
            item(id)
        }
        withContext(Dispatchers.IO) {
            svBatch.batchAll(DOMAIN, result) { sl ->
                val pureList = sl.filter { it.typeId in PokemonConst.TYPE_ID_SET }
                moveRepository.saveAll(pureList)
            }
        }
    }
    private suspend fun item(id:Int):EntMove {
        val item = pokemonWebClient.get()
            .uri("/${DOMAIN.apiKey}/$id/")
            .retrieve()
            .awaitBody<PokemonMoveDTO>()
        val nameByLang = item.names.associateBy({ it.language.name }, { it.name })
        val descriptionByLang = item.flavor_text_entries.associateBy({ it.language.name }, { it.flavor_text })
        val entity = EntMove(
            id = item.id,
            typeId = PokemonConst.getIdForUrl(item.type.url) ?: throw Throwable("unknown type id"),
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: item.name,
            descriptionEn = descriptionByLang[EnumLanguage.EN.key] ?: "",
            descriptionKr = descriptionByLang[EnumLanguage.KR.key] ?: "",
            descriptionJp = descriptionByLang[EnumLanguage.JP.key] ?: descriptionByLang[EnumLanguage.JP2.key] ?: "",
            details = mapper.writeValueAsString(mapOf(
                "accuracy" to item.accuracy,
                "power" to item.power,
                "pp" to item.pp,
                "damageClass" to (PokemonConst.getIdForUrl(item.damage_class.url) ?: throw Throwable("unknown damage_class id"))
            ))
        )
        println(entity)
        return entity
    }
}