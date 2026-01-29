package server.gateway

import server.dto.DTOType

interface GwType {
    suspend fun findAll(): List<DTOType>
    suspend fun deleteAll()
    suspend fun saveAll(list: List<DTOType>)
}