@file:Suppress("OPT_IN_USAGE")

package scan.util.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.CancellationException

suspend inline fun <T : Any, V : Any> executeOnce(
    items: List<T>,
    concurrency: Int,
    crossinline block: suspend (T) -> V?
): BatchResult<T, V> {
    val successList = ArrayList<BatchResultData.Success<T, V>>(items.size)
    val failList = ArrayList<BatchResultData.Fail<T, V>>(items.size)
    items.asFlow().flatMapMerge(concurrency) { item ->
        flow {
            val r = try {
                block(item)?.let { BatchResultData.Success(item, it) } ?: BatchResultData.Fail(item)
            } catch (e: CancellationException) {
                throw e
            }  catch (e: Throwable) {
                BatchResultData.Fail(item, e)
            }
            emit(r)
        }
    }.collect { r ->
        when (r) {
            is BatchResultData.Success -> successList.add(r)
            is BatchResultData.Fail -> failList.add(r)
        }
    }
    return BatchResult(successList, failList)
}

suspend inline fun <T : Any, V : Any> retryLoop(
    items: List<T>,
    retry: Int,
    concurrency: Int,
    delayMs: Long,
    multiplier: Double,
    crossinline block: suspend (T) -> V?
): BatchResult<T, V> {
    require(retry > 0) { "retry must be >= 1" }
    var pending: List<T> = items
    val successAll = mutableListOf<BatchResultData.Success<T, V>>()
    var failAll = emptyList<BatchResultData.Fail<T, V>>()
    var backoff = delayMs.toDouble()

    repeat(retry) { attempt ->
        if(attempt != 1) println("[RETRY]...$attempt")
        if (pending.isEmpty()) return BatchResult(successAll, emptyList())

        val (success, failed) = executeOnce(pending, concurrency, block)
        successAll += success
        pending = failed.map { it.item }
        failAll = failed

        if (pending.isNotEmpty() && delayMs > 0 && attempt < retry - 1) {
            delay(backoff.toLong())
            backoff *= multiplier
        }
    }
    return BatchResult(successAll, failAll)
}

data class BatchResult<T, V>(
    val successList: List<BatchResultData.Success<T, V>>,
    val failList: List<BatchResultData.Fail<T, V>>
) {
    val isAllSuccess: Boolean get() = failList.isEmpty()
    val successCount: Int get() = successList.size
    val failCount: Int get() = failList.size

    fun successData(): List<V> = successList.map { it.data }
    fun failedItems(): List<T> = failList.map { it.item }
}


sealed class BatchResultData<T, V> {
    data class Success<T, V>(val item: T, val data:V) : BatchResultData<T, V>()
    data class Fail<T, V>(val item: T, val error: Throwable? = null) : BatchResultData<T, V>()
}