package server.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import server.entity.jpa.EntMove

interface RepoMoveJpa : JpaRepository<EntMove, Int> {
    fun findByNameEnIgnoreCase(nameEn: String): EntMove?
}