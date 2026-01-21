package scan.util.coroutine

suspend inline fun <T : Any, V : Any> Iterable<T>.awaitAll(
    concurrency: Int = 16,
    crossinline block: suspend (T) -> V?
): BatchResult<T, V> {
    return executeOnce(toList(), concurrency, block)
}

suspend inline fun <T : Any, V : Any> Iterable<T>.retryAwaitAll(
    retry: Int,
    concurrency: Int = 16,
    delayMs: Long = 0,
    multiplier: Double = 1.0,
    crossinline block: suspend (T) -> V?
): BatchResult<T, V> = retryLoop(toList(), retry, concurrency, delayMs, multiplier, block)