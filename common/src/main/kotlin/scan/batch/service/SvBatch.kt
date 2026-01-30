package scan.batch.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import scan.batch.dto.DTOFail
import scan.batch.dto.DTOFailDetail
import scan.batch.gateway.GwFail
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.dto.PokemonErrorDTO
import scan.tx.TxRunner

@Service
class SvBatch(
    private val failGateway: GwFail,
    private val txRunner: TxRunner
) {
    companion object {
        private const val BATCH_CHUNK_SIZE = 200
    }
    private val log = LoggerFactory.getLogger(javaClass)
    suspend fun findAllFail(domain: EnumFailDomain):List<DTOFail> = failGateway.findAllByDomain(domain)
    suspend fun <V> batchAll(domain:EnumFailDomain, result:BatchResult<Int, V>, successBlock:suspend (List<V>)->Unit) {
        log.info("[배치 진행중] 도메인: ${domain.name} (성공:${result.successCount}/실패:${result.failCount})")
        txRunner.tx {
            if(result.successList.isNotEmpty()) {
                log.info("[배치 진행중] 성공 리스트 처리 시작")
                val data = result.successData()
                data.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                    successBlock(chunk)
                }
                log.info("[배치 진행중] 성공 리스트 처리 완료")
                log.info("[배치 진행중] 성공 리스트 -> 실패 리스트 삭제 처리 시작")
                val refIds = result.successList.map { it.item }
                refIds.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                    failGateway.deleteAllByDomainAndRefIdIn(domain, chunk)
                }
                log.info("[배치 진행중] 성공 리스트 -> 실패 리스트 삭제 처리 완료")
            }
            if(result.failList.isNotEmpty()) {
                val failDTOs = result.failList.map { PokemonErrorDTO(domain, it.item, it.error ?: Throwable("unknown error")) }
                log.info("[배치 진행중] 실패 리스트 처리 시작")
                failDTOs.chunked(BATCH_CHUNK_SIZE).forEach { chunk ->
                    reportAll(chunk)
                }
                log.info("[배치 진행중] 실패 리스트 처리 완료")
            }
        }
        log.info("[배치 완료] 도메인: ${domain.name}")
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