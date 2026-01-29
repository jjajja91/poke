package server.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import server.entity.jpa.EntType

interface RepoTypeJpa : JpaRepository<EntType, Int>