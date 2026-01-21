package server.repository

import org.springframework.data.jpa.repository.JpaRepository
import server.entity.EntVersion

interface RepoVersion : JpaRepository<EntVersion, Int>