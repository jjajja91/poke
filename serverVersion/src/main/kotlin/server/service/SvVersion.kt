package server.service

import org.springframework.stereotype.Service
import scan.batch.service.BatchStrategy
import scan.batch.service.SvBatchJobRunner
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonApiCatalog
import server.dto.DTOVersion
import server.gateway.GwVersion

@Service
class SvVersion(
    private val versionGateway: GwVersion,
    private val pokemonApiCatalog: PokemonApiCatalog,
    private val pokemonApiVersion: PokemonApiVersion,
    private val batchJobRunner: SvBatchJobRunner
) {
    private val versionBatchStrategy = object : BatchStrategy<DTOVersion> {
        override val domain = EnumFailDomain.VERSION

        override suspend fun getIdSet(): Set<Int> = pokemonApiCatalog.fetchIdSet(domain.apiKey)

        override suspend fun fetchData(idSet: Set<Int>): BatchResult<Int, DTOVersion> =
            if (idSet.isEmpty()) BatchResult(emptyList(), emptyList())
            else idSet.retryAwaitAll(
                retry = 3,
                concurrency = 6,
                delayMs = 3000
            ) { id ->
                pokemonApiVersion.fetchVersion(id)
            }

        override suspend fun saveData(list: List<DTOVersion>) {
            versionGateway.saveAll(list)
        }

        override suspend fun clearData() {
            versionGateway.deleteAll()
        }
    }
    suspend fun addAllForce(): String = batchJobRunner.startBatchJob(
        strategy = versionBatchStrategy,
        isForce = true
    )
    suspend fun addAllCheck(): String = batchJobRunner.startBatchJob(
        strategy = versionBatchStrategy,
        isForce = false
    )
}