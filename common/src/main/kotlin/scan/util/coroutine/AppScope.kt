package scan.util.coroutine

import jakarta.annotation.PreDestroy
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

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext =
        job + Dispatchers.Default + CoroutineName("app-scope")

    fun launchApp(
        name: String,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        return launch(CoroutineName(name)) {
            try {
                block()
            } catch (ce: CancellationException) {
                logger.warn("Job cancelled: {}", name)
                throw ce
            } catch (t: Throwable) {
                logger.error("Job failed: {}", name, t)
                // SupervisorJob이라 여기서 throw해도 다른 작업엔 영향 없음
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
