package server.service

import org.springframework.stereotype.Service
import scan.batch.service.BatchStrategy
import scan.batch.service.SvBatchJobRunner
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonConst
import server.dto.DTOType
import server.gateway.GwType

@Service
class SvType(
    private val typeGateway: GwType,
    private val pokemonApiType: PokemonApiType,
    private val batchJobRunner: SvBatchJobRunner
) {
    private val batchStrategy = object : BatchStrategy<DTOType> {
        override val domain = EnumFailDomain.TYPE

        override suspend fun getIdSet(): Set<Int> = PokemonConst.TYPE_ID_SET

        override suspend fun fetchData(idSet: Set<Int>): BatchResult<Int, DTOType> {
            return if (idSet.isEmpty()) BatchResult(emptyList(), emptyList())
            else idSet.retryAwaitAll(
                retry = 3,
                concurrency = 6,
                delayMs = 3000
            ) { id ->
                pokemonApiType.fetch(id)
            }
        }

        override suspend fun saveData(list: List<DTOType>) {
            typeGateway.saveAll(list)
        }

        override suspend fun clearData() {
            typeGateway.deleteAll()
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