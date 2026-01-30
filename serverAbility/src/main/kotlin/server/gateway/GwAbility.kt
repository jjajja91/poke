package server.gateway

import server.dto.DTOAbility

interface GwAbility {
    suspend fun findAll(): List<DTOAbility>
    suspend fun deleteAll()
    suspend fun saveAll(list: List<DTOAbility>)
}