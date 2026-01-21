package server.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.batch.SvBatch
import server.entity.EntAbility
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import server.repository.RepoAbility
import scan.util.coroutine.AppScope
import scan.util.coroutine.JobStatusStore
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonConst
import server.dto.PokemonAbilityDTO
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@Service
class SvAbility(
    private val abilityRepository: RepoAbility,
    private val pokemonWebClient: WebClient,
    private val svBatch: SvBatch,
    private val appScope: AppScope,
    private val statusStore: JobStatusStore
) {
    private val DOMAIN = EnumFailDomain.ABILITY
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
                abilityRepository.deleteAllInBatch()
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
                    abilityRepository.saveAll(sl)
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
                abilityRepository.saveAll(sl)
            }
        }
    }
    private suspend fun item(id:Int):EntAbility {
        val item = pokemonWebClient.get()
            .uri("/${DOMAIN.apiKey}/$id/")
            .retrieve()
            .awaitBody<PokemonAbilityDTO>()
        val nameByLang = item.names.associateBy({ it.language.name }, { it.name })
        val descriptionByLang = item.flavor_text_entries.associateBy({ it.language.name }, { it.flavor_text })
        val entity = EntAbility(
            id = item.id,
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: item.name,
            descriptionEn = descriptionByLang[EnumLanguage.EN.key] ?: "",
            descriptionKr = descriptionByLang[EnumLanguage.KR.key] ?: "",
            descriptionJp = descriptionByLang[EnumLanguage.JP.key] ?: descriptionByLang[EnumLanguage.JP2.key] ?: "",
        )
        println(entity)
        return entity
    }
}