package server.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.service.SvAbility
import scan.util.coroutine.JobStatusStore

@RestController
@RequestMapping("/api/ability")
class CtrlAbility(
    private val service: SvAbility,
    private val statusStore: JobStatusStore
) {
    @PostMapping("/add/all")
    suspend fun addAll() { service.addAllCheck() }

    @PostMapping("/add/all/force")
    fun addAllForce(): ResponseEntity<Map<String, String>> {
        val jobId = service.startAddAllForce()
        return ResponseEntity.accepted().body(mapOf("jobId" to jobId))
    }
    @GetMapping("/job/{jobId}")
    fun job(@PathVariable jobId: String): ResponseEntity<Any> {
        val status = statusStore.get(jobId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(status)
    }
}