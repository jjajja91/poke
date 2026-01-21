package scan.batch

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.dto.PokemonErrorDTO

@Service
class SvBatch(
    private val failRepository: RepoFail,
    private val mapper: ObjectMapper
) {
    companion object {
        private const val BATCH_CHUNK_SIZE = 200
    }
    suspend fun findAllFail(domain: EnumFailDomain):List<EntFail> = withContext(Dispatchers.IO) { failRepository.findAllByDomain(domain.tableName) }
    @Transactional
    fun <V> batchAll(domain:EnumFailDomain, result:BatchResult<Int, V>, successBlock:(List<V>)->Unit) {
        if(result.successList.isNotEmpty()) {
            result.successData().chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                successBlock(chunk)
            }
            val refIds = result.successList.map { it.item }
            refIds.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                failRepository.deleteAllByDomainAndRefIdIn(domain.tableName, chunk)
            }
        }
        if(result.failList.isNotEmpty()) {
            val failDTOs = result.failList.map { PokemonErrorDTO(domain, it.item, it.error ?: Throwable("unknown error")) }
            failDTOs.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                reportAll(chunk)
            }
        }
    }
    private fun reportAll(items: List<PokemonErrorDTO>) {
        if (items.isEmpty()) return
        val rows = items.map { item ->
            FailRow(
                domain = item.domain.tableName,
                id = item.id,
                errorJson = toErrorJson(item.error)
            )
        }
        failRepository.upsertAll(rows)
    }
    @Transactional
    fun deleteAllFail(domain: EnumFailDomain) {
        failRepository.deleteAllByDomain(domain.tableName)
    }
    private fun toErrorJson(error: Throwable) = mapper.writeValueAsString(
        mapOf(
            "message" to (error.message ?: error.localizedMessage),
            "type" to error::class.simpleName,
            "cause" to (error.cause?.javaClass?.simpleName ?: "")
        )
    )
}