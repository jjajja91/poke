package server.service

import org.springframework.stereotype.Service
import scan.batch.service.BatchStrategy
import scan.batch.service.SvBatchJobRunner
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonApiCatalog
import server.dto.DTOMove
import server.gateway.GwMove

@Service
class SvMove(
    private val moveGateway: GwMove,
    private val pokemonApiCatalog: PokemonApiCatalog,
    private val pokemonApiMove: PokemonApiMove,
    private val batchJobRunner: SvBatchJobRunner
) {
    private val batchStrategy = object : BatchStrategy<DTOMove> {
        override val domain = EnumFailDomain.MOVE

        override suspend fun getIdSet(): Set<Int> = pokemonApiCatalog.fetchIdSet(domain.apiKey)

        override suspend fun fetchData(idSet: Set<Int>): BatchResult<Int, DTOMove> {
            return if (idSet.isEmpty()) BatchResult(emptyList(), emptyList())
            else idSet.retryAwaitAll(
                retry = 3,
                concurrency = 6,
                delayMs = 3000
            ) { id ->
                pokemonApiMove.fetch(id)
            }
        }

        override suspend fun saveData(list: List<DTOMove>) {
            moveGateway.saveAll(list)
        }

        override suspend fun clearData() {
            moveGateway.deleteAll()
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