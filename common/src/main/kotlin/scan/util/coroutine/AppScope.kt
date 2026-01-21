package scan.util.coroutine

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

@Component
class AppScope : CoroutineScope {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val job: CompletableJob = SupervisorJob()

    override val coroutineContext: CoroutineContext =
        job + Dispatchers.Default + CoroutineName("app-scope")

    fun launchApp(
        name: String,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        // ✅ 기존 coroutineContext(job 포함)에 dispatcher + name을 "추가"해서 실행
        return launch(coroutineContext + dispatcher + CoroutineName(name)) {
            try {
                block()
            } catch (ce: CancellationException) {
                logger.warn("Job cancelled: {}", name)
                throw ce
            } catch (t: Throwable) {
                logger.error("Job failed: {}", name, t)
                throw t
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        logger.info("AppScope shutdown: cancelling jobs")
        job.cancel()
    }
}