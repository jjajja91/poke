package server.repository

import org.springframework.data.jpa.repository.JpaRepository
import server.entity.EntAbility

interface RepoAbility : JpaRepository<EntAbility, Int>