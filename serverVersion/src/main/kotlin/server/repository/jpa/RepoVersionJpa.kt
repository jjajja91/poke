package server.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import server.entity.jpa.EntVersion

interface RepoVersionJpa : JpaRepository<EntVersion, Int>