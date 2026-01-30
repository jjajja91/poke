package server.repository.jpa

import org.springframework.data.jpa.repository.JpaRepository
import server.entity.jpa.EntAbility

interface RepoAbilityJpa : JpaRepository<EntAbility, Int>