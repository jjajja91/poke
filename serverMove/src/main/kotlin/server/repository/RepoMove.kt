package server.repository

import org.springframework.data.jpa.repository.JpaRepository
import server.entity.EntMove

interface RepoMove : JpaRepository<EntMove, Int> {
    fun findByNameEnIgnoreCase(nameEn: String): EntMove?
}