package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOPokemon(
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
    val details: DTOPokemonDetails,
    val regDate: LocalDateTime? = null
): DTO

data class DTOPokemonDetails(
    val abilityRowids: List<Int>,
    val moveRowids: List<Int>,
    val eggGroupIds: List<Int>,
    val isDefault: Boolean,
    val order: Int,
    val sprites: Any? = null,
    val descriptionEn: String,
    val descriptionKr: String,
    val descriptionJp: String,
    val formSwitchable: Boolean,
    val forms: List<String>,
    val generaEn: String,
    val generaKr: String,
    val generaJp: String,
    val hasGenderDifferences: Boolean,
    val isBaby: Boolean,
    val isLegendary: Boolean,
    val isMythical: Boolean
): DTO