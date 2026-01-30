package server.gateway

import server.dto.DTOPokemon

interface GwPokemon {
    suspend fun findAll(): List<DTOPokemon>
    suspend fun deleteAll()
    suspend fun saveAll(list: List<DTOPokemon>)
}