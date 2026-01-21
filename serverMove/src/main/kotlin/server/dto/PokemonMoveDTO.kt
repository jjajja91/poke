package server.dto

import scan.dto.PokemonEntryDTO
import scan.dto.PokemonNameLanguageDTO
import scan.dto.PokemonNameUrlDTO

data class PokemonMoveDTO(
    val id: Int,
    val name: String,
    val names: List<PokemonNameLanguageDTO>,
    val flavor_text_entries: List<PokemonEntryDTO>,
    val damage_class: PokemonNameUrlDTO,
    val accuracy: Int,
    val power: Int,
    val pp: Int,
    val type: PokemonNameUrlDTO
)
