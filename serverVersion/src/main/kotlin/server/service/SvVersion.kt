package server.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.batch.SvBatch
import server.entity.EntVersion
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import server.repository.RepoVersion
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonConst
import server.dto.PokemonVersionDTO

@Service
class SvVersion(
    private val versionRepository: RepoVersion,
    private val pokemonWebClient: WebClient,
    private val svBatch: SvBatch
) {
    private val DOMAIN = EnumFailDomain.VERSION
    suspend fun addAllForce() {
        deleteAll()
        withContext(Dispatchers.IO) {
            svBatch.deleteAllFail(DOMAIN)
        }
        addList(PokemonConst.getIdSet(pokemonWebClient, DOMAIN.apiKey))
    }
    suspend fun addAllCheck() {
        val failList = svBatch.findAllFail(DOMAIN)
        addList(failList.map { it.refId }.toSet())
    }
    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            versionRepository.deleteAllInBatch()
        }
    }
    private suspend fun addList(idSet:Set<Int>) {
        if (idSet.isEmpty()) return
        val result = idSet.retryAwaitAll(
            retry = 3, concurrency = 7, delayMs = 3000
        ) { id ->
            item(id)
        }
        withContext(Dispatchers.IO) {
            svBatch.batchAll(DOMAIN, result) {
                versionRepository.saveAll(it)
            }
        }
    }
    private suspend fun item(id:Int):EntVersion {
        val item = pokemonWebClient.get()
            .uri("/${DOMAIN.apiKey}/$id/")
            .retrieve()
            .awaitBody<PokemonVersionDTO>()
        val nameByLang = item.names.associateBy({ it.language.name }, { it.name })
        val entity = EntVersion(
            id = item.id,
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: item.name,
            groupKey = item.version_group.name
        )
        println(entity)
        return entity
    }
}