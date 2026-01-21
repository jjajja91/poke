package server.repository

import org.springframework.data.jpa.repository.JpaRepository
import server.entity.EntType

interface RepoType : JpaRepository<EntType, Int>