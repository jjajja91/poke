package server.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.service.SvMove
import scan.util.coroutine.JobStatusStore

@RestController
@RequestMapping("/api/move")
class CtrlMove(
    private val service: SvMove,
    private val statusStore: JobStatusStore
) {
    @PostMapping("/add/all")
    suspend fun addAll(): ResponseEntity<Map<String, String>> {
        val jobId = service.addAllCheck()
        return ResponseEntity.accepted().body(mapOf("jobId" to jobId))
    }

    @PostMapping("/add/all/force")
    suspend fun addAllForce(): ResponseEntity<Map<String, String>> {
        val jobId = service.addAllForce()
        return ResponseEntity.accepted().body(mapOf("jobId" to jobId))
    }
    @GetMapping("/job/{jobId}")
    suspend fun job(@PathVariable jobId: String): ResponseEntity<Any> {
        val status = statusStore.get(jobId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(status)
    }
}