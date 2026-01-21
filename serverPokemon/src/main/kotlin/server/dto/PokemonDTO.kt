package server.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import scan.dto.PokemonEntryDTO
import scan.dto.PokemonNameLanguageDTO
import scan.dto.PokemonNameUrlDTO


data class PokemonApiDTO(
    val id: Int, // 고유 식별자
    val abilities: List<PokemonApiAbilityDTO>, // 포켓몬 특성
    val forms: List<PokemonNameUrlDTO>, // 포켓몬 폼
    val game_indices:List<PokemonApiGameIndices>, // 버전별 정보
    val is_default: Boolean, // ??
    val moves: List<PokemonApiMoveDTO>, // 포켓몬 기술
    val name: String, // 포켓몬 이름
    val order: Int, // ??
    val species: PokemonNameUrlDTO, // 디테일 정보
    val sprites: PokemonApiImageDTO, // 포켓몬 이미지
    val stats: List<PokemonApiStatDTO>, // 포켓몬 스탯
    val types: List<PokemonApiTypesDTO> // 포켓몬 타입
)

data class PokemonApiAbilityDTO(
    val ability: PokemonNameUrlDTO,
    val is_hidden: Boolean,
    val slot: Int
)

data class PokemonApiGameIndices(
    val game_index: Int,
    val version: PokemonNameUrlDTO
)

data class PokemonApiMoveDTO(
    val move: PokemonNameUrlDTO,
    val version_group_details: List<PokemonApiMoveVersionDTO>
)

data class PokemonApiMoveVersionDTO(
    val level_learned_at: Int,
    val move_learn_method: PokemonNameUrlDTO,
    val order: Int?,
    val version_group: PokemonNameUrlDTO
)

data class PokemonApiImageDTO(
    val front_default: String?,
    val front_shiny: String?
)

data class PokemonApiStatDTO(
    val base_stat: Int,
    val stat: PokemonNameUrlDTO
)

data class PokemonApiTypesDTO(
    val slot: Int,
    val type: PokemonNameUrlDTO
)

data class PokemonSpeciesDTO(
    val id: Int, // 고유 식별자
    val egg_groups: List<PokemonNameUrlDTO>,
    val evolves_from_species: PokemonNameUrlDTO?,
    val flavor_text_entries:List<PokemonEntryDTO>,
    val forms_switchable: Boolean,
    val genera: List<PokemonGeneraDTO>,
    val has_gender_differences:Boolean,
    val is_baby: Boolean,
    val is_legendary:Boolean,
    val is_mythical:Boolean,
    val name:String,
    val names: List<PokemonNameLanguageDTO>,
    val order: Int
)

data class PokemonGeneraDTO(
    val genus: String,
    val language: PokemonNameUrlDTO
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PokemonDetailsDTO(
    val abilityRowids: List<Int> = emptyList(),
    val moveRowids: List<Int> = emptyList(),
    val eggGroupIds: List<Int> = emptyList(),

    val isDefault: Boolean = false,
    val order: Int = 0,

    val sprites: Any? = null,

    val originRowid: Int? = null,

    val descriptionEn: String = "",
    val descriptionKr: String = "",
    val descriptionJp: String = "",

    val formSwitchable: Boolean = false,
    val forms: List<String> = emptyList(),

    val generaEn: String = "",
    val generaKr: String = "",
    val generaJp: String = "",

    val hasGenderDifferences: Boolean = false,
    val isBaby: Boolean = false,
    val isLegendary: Boolean = false,
    val isMythical: Boolean = false
)