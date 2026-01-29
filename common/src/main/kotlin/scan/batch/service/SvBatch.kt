package scan.batch.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import scan.batch.dto.DTOFail
import scan.batch.dto.DTOFailDetail
import scan.batch.gateway.GwFail
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.dto.PokemonErrorDTO

@Service
class SvBatch(
    private val failGateway: GwFail
) {
    companion object {
        private const val BATCH_CHUNK_SIZE = 200
    }
    suspend fun findAllFail(domain: EnumFailDomain):List<DTOFail> = withContext(Dispatchers.IO) { failGateway.findAllByDomain(domain) }
    suspend fun <V> batchAll(domain:EnumFailDomain, result:BatchResult<Int, V>, successBlock:suspend (List<V>)->Unit) {
        if(result.successList.isNotEmpty()) {
            val data = result.successData()
            data.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                successBlock(chunk)
            }
            val refIds = result.successList.map { it.item }
            refIds.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                failGateway.deleteAllByDomainAndRefIdIn(domain, chunk)
            }
        }
        if(result.failList.isNotEmpty()) {
            val failDTOs = result.failList.map { PokemonErrorDTO(domain, it.item, it.error ?: Throwable("unknown error")) }
            failDTOs.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                reportAll(chunk)
            }
        }
    }
    private suspend fun reportAll(items: List<PokemonErrorDTO>) {
        if (items.isEmpty()) return
        val rows = items.map { item ->
            DTOFail(
                domain = item.domain,
                refId = item.id,
                error = toFailDetail(item.error)
            )
        }
        failGateway.saveAll(rows)
    }
    suspend fun deleteAllFail(domain: EnumFailDomain) {
        failGateway.deleteAllByDomain(domain)
    }
    private fun toFailDetail(error: Throwable) = DTOFailDetail(
        message = error.message ?: error.localizedMessage,
        type = error::class.simpleName ?: "",
        cause = error.cause?.javaClass?.simpleName ?: ""
    )
}