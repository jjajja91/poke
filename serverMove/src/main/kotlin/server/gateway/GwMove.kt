package server.gateway

import server.dto.DTOMove

interface GwMove {
    suspend fun findAll(): List<DTOMove>
    suspend fun deleteAll()
    suspend fun saveAll(list: List<DTOMove>)
}