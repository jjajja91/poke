package server.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.batch.SvBatch
import scan.batch.SvBatchJobRunner
import server.entity.EntMove
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import server.repository.RepoMove
import scan.util.coroutine.BatchResult
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonConst
import scan.util.pokemon.toJson
import server.dto.PokemonMoveDTO
import server.dto.PokemonMoveDetailDTO

@Service
class SvMove(
    private val moveRepository: RepoMove,
    private val mapper: ObjectMapper,
    private val pokemonWebClient: WebClient,
    private val svBatch: SvBatch,
    private val svBatchJobRunner: SvBatchJobRunner
) {
    private val DOMAIN = EnumFailDomain.MOVE
    fun getName(name: String): EntMove {
        val move = moveRepository.findByNameEnIgnoreCase(name) ?: throw Throwable("Move $name not found")
        return move
    }
    fun addAllForce():String {
        return svBatchJobRunner.startBatchJob(
            DOMAIN,
            {
                moveRepository.deleteAllInBatch()
                svBatch.deleteAllFail(DOMAIN)
                val idSet = PokemonConst.getIdSet(pokemonWebClient, DOMAIN.apiKey)
                addList(idSet)
            }, { sl ->
                val pureList = sl.filter { it.typeId in PokemonConst.TYPE_ID_SET }
                moveRepository.saveAll(pureList)
            }
        )
    }
    suspend fun addAllCheck():String {
        return svBatchJobRunner.startBatchJob(
            DOMAIN,
            {
                val failList = svBatch.findAllFail(DOMAIN)
                addList(failList.map { it.refId }.toSet())
            }, { sl ->
                moveRepository.saveAll(sl)
            }
        )
    }
    private suspend fun addList(idSet:Set<Int>): BatchResult<Int, EntMove> {
        return if (idSet.isEmpty()) BatchResult(listOf(), listOf())
        else idSet.retryAwaitAll(
            retry = 3, concurrency = 7, delayMs = 3000
        ) { id ->
            item(id)
        }
    }
    private suspend fun item(id:Int):EntMove {
        val item = pokemonWebClient.get()
            .uri("/${DOMAIN.apiKey}/$id/")
            .retrieve()
            .awaitBody<PokemonMoveDTO>()
        val nameByLang = item.names.associateBy({ it.language.name }, { it.name })
        val descriptionByLang = item.flavor_text_entries.associateBy({ it.language.name }, { it.flavor_text })
        val detail = PokemonMoveDetailDTO(
            accuracy = item.accuracy,
            power = item.power,
            pp = item.pp,
            damageClass = PokemonConst.getIdForUrl(item.damage_class.url) ?: throw Throwable("unknown damage_class id")
        )
        val entity = EntMove(
            id = item.id,
            typeId = PokemonConst.getIdForUrl(item.type.url) ?: throw Throwable("unknown type id"),
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: item.name,
            descriptionEn = descriptionByLang[EnumLanguage.EN.key] ?: "",
            descriptionKr = descriptionByLang[EnumLanguage.KR.key] ?: "",
            descriptionJp = descriptionByLang[EnumLanguage.JP.key] ?: descriptionByLang[EnumLanguage.JP2.key] ?: "",
            details = mapper.toJson(detail)
        )
        println(entity)
        return entity
    }
}