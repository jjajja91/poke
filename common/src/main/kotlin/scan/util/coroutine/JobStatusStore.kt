package scan.util.coroutine

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class JobStatusStore {

    data class Status(
        val jobId: String,
        val name: String,
        val total: Int = 0,
        val success: Int = 0,
        val fail: Int = 0,
        val state: State = State.RUNNING,
        val message: String? = null
    )

    enum class State { RUNNING, DONE, FAILED }

    private val map = ConcurrentHashMap<String, Status>()

    fun start(jobId: String, name: String, total: Int = 0) {
        map[jobId] = Status(jobId = jobId, name = name, total = total, state = State.RUNNING)
    }

    fun update(jobId: String, total: Int? = null, success: Int? = null, fail: Int? = null, message: String? = null) {
        map.computeIfPresent(jobId) { _, cur ->
            cur.copy(
                total = total ?: cur.total,
                success = success ?: cur.success,
                fail = fail ?: cur.fail,
                message = message ?: cur.message
            )
        }
    }

    fun done(jobId: String, success: Int, fail: Int) {
        map.computeIfPresent(jobId) { _, cur ->
            cur.copy(success = success, fail = fail, state = State.DONE)
        }
    }

    fun failed(jobId: String, success: Int, fail: Int, message: String?) {
        map.computeIfPresent(jobId) { _, cur ->
            cur.copy(success = success, fail = fail, state = State.FAILED, message = message)
        }
    }

    fun get(jobId: String): Status? = map[jobId]
}