package server.service

import org.springframework.stereotype.Service
import scan.batch.service.BatchStrategy
import scan.batch.service.SvBatchJobRunner
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonApiCatalog
import server.dto.DTOAbility
import server.gateway.GwAbility

@Service
class SvAbility(
    private val abilityGateway: GwAbility,
    private val pokemonApiCatalog: PokemonApiCatalog,
    private val pokemonApiAbility: PokemonApiAbility,
    private val batchJobRunner: SvBatchJobRunner
) {
    private val batchStrategy = object : BatchStrategy<DTOAbility> {
        override val domain = EnumFailDomain.ABILITY

        override suspend fun getIdSet(): Set<Int> = pokemonApiCatalog.fetchIdSet(domain.apiKey)

        override suspend fun fetchData(idSet: Set<Int>): BatchResult<Int, DTOAbility> =
            if (idSet.isEmpty()) BatchResult(emptyList(), emptyList())
            else idSet.retryAwaitAll(
                retry = 3,
                concurrency = 6,
                delayMs = 3000
            ) { id ->
                pokemonApiAbility.fetch(id)
            }

        override suspend fun saveData(list: List<DTOAbility>) {
            abilityGateway.saveAll(list)
        }

        override suspend fun clearData() {
            abilityGateway.deleteAll()
        }
    }
    suspend fun addAllForce(): String = batchJobRunner.startBatchJob(
        strategy = batchStrategy,
        isForce = true
    )
    suspend fun addAllCheck(): String = batchJobRunner.startBatchJob(
        strategy = batchStrategy,
        isForce = false
    )
}