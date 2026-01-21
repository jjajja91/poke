package scan.batch

import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service
import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult
import scan.util.coroutine.AppScope
import scan.util.coroutine.JobStatusStore
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@Service
class SvBatchJobRunner(
    private val appScope: AppScope,
    private val statusStore: JobStatusStore,
    private val svBatch: SvBatch
) {
    fun <V> startBatchJob(
        domain: EnumFailDomain,
        resultBlock:suspend () -> BatchResult<Int, V>,
        saveBlock:(List<V>) -> Unit
    ): String {
        val jobId = UUID.randomUUID().toString()
        val name = "${domain.name}:batchJob"
        statusStore.start(jobId, name = name)
        appScope.launchApp("$name:$jobId", Dispatchers.IO) {
            runBatchJob(jobId, domain, resultBlock, saveBlock)
        }
        return jobId
    }
    private suspend fun <V> runBatchJob(
        jobId: String,
        domain: EnumFailDomain,
        resultBlock:suspend() -> BatchResult<Int, V>,
        saveBlock:(List<V>) -> Unit
    ) {
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        try {
            val result = resultBlock()
            successCount.set(result.successList.size)
            failCount.set(result.failList.size)
            svBatch.batchAll(domain, result) { sl ->
                saveBlock(sl)
            }
            statusStore.done(jobId, success = successCount.get(), fail = failCount.get())
        } catch (t: Throwable) {
            statusStore.failed(jobId, success = successCount.get(), fail = failCount.get(), message = t.message)
            throw t
        }
    }
}