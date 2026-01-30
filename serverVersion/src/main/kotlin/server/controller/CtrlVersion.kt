package server.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scan.util.coroutine.JobStatusStore
import server.service.SvVersion

@RestController
@RequestMapping("/api/version")
class CtrlVersion(
    private val service: SvVersion,
    private val statusStore: JobStatusStore
) {
    @PostMapping("/add/all")
    suspend fun addAll() { service.addAllCheck() }

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