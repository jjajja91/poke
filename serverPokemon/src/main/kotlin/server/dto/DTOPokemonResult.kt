package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOPokemonResult(
    val pokemonRowid: Int,
    val type1Rowid: Int,
    val type2Rowid: Int,
    val baseRowid: Int,
    val nameKr: String,
    val nameJp: String,
    val nameEn: String,
    val hp: Int,
    val atk: Int,
    val satk: Int,
    val spd: Int,
    val def: Int,
    val sdef: Int,
    val details: String,
    val regDate: LocalDateTime
): DTO