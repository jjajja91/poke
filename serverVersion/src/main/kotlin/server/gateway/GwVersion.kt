package server.gateway

import server.dto.DTOVersion

interface GwVersion {
    suspend fun findAll(): List<DTOVersion>
    suspend fun deleteAll()
    suspend fun saveAll(list: List<DTOVersion>)
}