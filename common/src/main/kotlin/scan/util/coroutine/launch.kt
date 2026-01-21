package scan.util.coroutine

suspend inline fun <T : Any> Iterable<T>.launchAll(
    concurrency: Int = 16,
    crossinline block: suspend (T) -> Unit
):BatchResult<T, Unit> = executeOnce(toList(), concurrency) { block(it) }

suspend inline fun <T : Any> Iterable<T>.retryLaunchAll(
    retry: Int,
    concurrency: Int = 16,
    delayMs: Long = 0,
    multiplier: Double = 1.0,
    crossinline block: suspend (T) -> Unit
): BatchResult<T, Unit> = retryLoop(toList(), retry, concurrency, delayMs, multiplier) { block(it) }