package server.dto

import scan.dto.PokemonNameLanguageDTO
import scan.dto.PokemonNameUrlDTO

data class PokemonVersionDTO(
    val id: Int,
    val name: String,
    val names: List<PokemonNameLanguageDTO>,
    val version_group: PokemonNameUrlDTO
)