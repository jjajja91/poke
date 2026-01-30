package server.dto

import scan.dto.PokemonEntryDTO
import scan.dto.PokemonNameLanguageDTO

data class DTOPokemonApiAbility(
    val id: Int,
    val name: String,
    val names: List<PokemonNameLanguageDTO>,
    val flavor_text_entries: List<PokemonEntryDTO>,
)