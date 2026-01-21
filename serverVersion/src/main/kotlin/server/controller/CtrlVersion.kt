package server.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.service.SvVersion

@RestController
@RequestMapping("/api/version")
class CtrlVersion(
    private val service: SvVersion
) {
    @PostMapping("/add/all")
    suspend fun addAll() { service.addAllCheck() }

    @PostMapping("/add/all/force")
    suspend fun addAllForce() { service.addAllForce() }
}