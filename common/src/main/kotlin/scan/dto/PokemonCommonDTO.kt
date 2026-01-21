package scan.dto

import scan.enum.EnumFailDomain

data class PokemonListDTO(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonNameUrlDTO>
)

data class PokemonNameUrlDTO(
    val name: String,
    val url: String
)

data class PokemonNameLanguageDTO(
    val name: String,
    val language: PokemonNameUrlDTO
)

data class PokemonEntryDTO(
    val flavor_text: String,
    val language: PokemonNameUrlDTO
)

data class PokemonErrorDTO(
    val domain: EnumFailDomain,
    val id: Int,
    val error: Throwable
)