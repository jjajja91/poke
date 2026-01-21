package server.dto

import scan.dto.PokemonNameLanguageDTO
import scan.dto.PokemonNameUrlDTO

data class PokemonTypeDTO(
    val id: Int,
    val name: String,
    val names: List<PokemonNameLanguageDTO>,
    val damage_relations: PokemonDamageRelationDTO
)

data class PokemonDamageRelationDTO(
    val double_damage_from: List<PokemonNameUrlDTO>,
    val double_damage_to: List<PokemonNameUrlDTO>,
    val half_damage_from: List<PokemonNameUrlDTO>,
    val half_damage_to: List<PokemonNameUrlDTO>,
    val no_damage_from: List<PokemonNameUrlDTO>,
    val no_damage_to: List<PokemonNameUrlDTO>,
)

data class PokemonTypeRelationDTO(
    var damageFrom: Double,
    var damageTo: Double
)