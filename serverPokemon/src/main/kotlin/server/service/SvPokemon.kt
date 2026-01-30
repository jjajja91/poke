package server.service

import org.springframework.stereotype.Service
import scan.batch.service.BatchStrategy
import scan.batch.service.SvBatchJobRunner
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonApiCatalog
import server.dto.DTOPokemon
import server.gateway.GwPokemon

@Service
class SvPokemon(
    private val pokemonGateway: GwPokemon,
    private val pokemonApiCatalog: PokemonApiCatalog,
    private val pokemonApiPokemon: PokemonApiPokemon,
    private val batchJobRunner: SvBatchJobRunner
) {
    private val batchStrategy = object : BatchStrategy<DTOPokemon> {
        override val domain = EnumFailDomain.POKEMON

        override suspend fun getIdSet(): Set<Int> = pokemonApiCatalog.fetchIdSet(domain.apiKey)

        override suspend fun fetchData(idSet: Set<Int>): BatchResult<Int, DTOPokemon> {
            return if (idSet.isEmpty()) BatchResult(emptyList(), emptyList())
            else idSet.retryAwaitAll(
                retry = 3,
                concurrency = 6,
                delayMs = 3000
            ) { id ->
                pokemonApiPokemon.fetch(id)
            }
        }

        override suspend fun saveData(list: List<DTOPokemon>) {
            pokemonGateway.saveAll(list)
        }

        override suspend fun clearData() {
            pokemonGateway.deleteAll()
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