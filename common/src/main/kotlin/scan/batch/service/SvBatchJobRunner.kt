package scan.batch.service

import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import scan.util.coroutine.AppScope
import scan.util.coroutine.JobStatusStore
import java.util.UUID

@Service
class SvBatchJobRunner(
    private val appScope: AppScope,
    private val statusStore: JobStatusStore,
    private val svBatch: SvBatch
) {

    private val log = LoggerFactory.getLogger(javaClass)
    fun <V> startBatchJob(strategy: BatchStrategy<V>, isForce: Boolean): String {
        val jobId = UUID.randomUUID().toString()
        val domain = strategy.domain
        val name = "${domain.name}:batchJob"
        statusStore.start(jobId, name = name)
        log.info("[배치 시작] 도메인:${domain.name}, ID: $jobId")
        appScope.launchApp("$name:$jobId", Dispatchers.IO) {
            runBatchJob(jobId, strategy, isForce)
        }
        return jobId
    }
    private suspend fun <V> runBatchJob(jobId: String, strategy: BatchStrategy<V>, isForce: Boolean) {
        val domain = strategy.domain
        try {
            if (isForce) {
                strategy.clearData()
                svBatch.deleteAllFail(domain)
            }
            val idSet = if (isForce) strategy.getIdSet() else svBatch.findAllFail(domain).map { it.refId }.toSet()
            log.info("[배치] 도메인:${domain.name}, ID: $jobId, ID: ${idSet.joinToString()}")
            if (idSet.isEmpty()) {
                statusStore.done(jobId, success = 0, fail = 0)
                return
            }
            val result = strategy.fetchData(idSet)
            svBatch.batchAll(domain, result) { strategy.saveData(it) }
            statusStore.done(jobId, success = result.successList.size, fail = result.failList.size)
        } catch (t: Throwable) {
            log.error("[배치 에러] 도메인:${domain.name}, ID: $jobId, ${t.message ?: t.localizedMessage}")
            statusStore.failed(jobId, success = 0, fail = 0, message = t.message)
            throw t
        }
    }
}