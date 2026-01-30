package server.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import scan.dto.PokemonEntryDTO
import scan.dto.PokemonNameLanguageDTO
import scan.dto.PokemonNameUrlDTO


data class DTOPokemonApiPokemon(
    val id: Int, // 고유 식별자
    val abilities: List<DTOPokemonApiPokemonAbility>, // 포켓몬 특성
    val forms: List<PokemonNameUrlDTO>, // 포켓몬 폼
    val game_indices:List<DTOPokemonApiGameIndices>, // 버전별 정보
    val is_default: Boolean, // ??
    val moves: List<DTOPokemonApiPokemonMove>, // 포켓몬 기술
    val name: String, // 포켓몬 이름
    val order: Int, // ??
    val species: PokemonNameUrlDTO, // 디테일 정보
    val sprites: DTOPokemonApiImage, // 포켓몬 이미지
    val stats: List<DTOPokemonApiStat>, // 포켓몬 스탯
    val types: List<DTOPokemonApiTypes> // 포켓몬 타입
)

data class DTOPokemonApiPokemonAbility(
    val ability: PokemonNameUrlDTO,
    val is_hidden: Boolean,
    val slot: Int
)

data class DTOPokemonApiGameIndices(
    val game_index: Int,
    val version: PokemonNameUrlDTO
)

data class DTOPokemonApiPokemonMove(
    val move: PokemonNameUrlDTO,
    val version_group_details: List<DTOPokemonApiMoveVersion>
)

data class DTOPokemonApiMoveVersion(
    val level_learned_at: Int,
    val move_learn_method: PokemonNameUrlDTO,
    val order: Int?,
    val version_group: PokemonNameUrlDTO
)

data class DTOPokemonApiImage(
    val front_default: String?,
    val front_shiny: String?
)

data class DTOPokemonApiStat(
    val base_stat: Int,
    val stat: PokemonNameUrlDTO
)

data class DTOPokemonApiTypes(
    val slot: Int,
    val type: PokemonNameUrlDTO
)

data class DTOPokemonApiSpecies(
    val id: Int, // 고유 식별자
    val egg_groups: List<PokemonNameUrlDTO>,
    val evolves_from_species: PokemonNameUrlDTO?,
    val flavor_text_entries:List<PokemonEntryDTO>,
    val forms_switchable: Boolean,
    val genera: List<DTOPokemonApiGenera>,
    val has_gender_differences:Boolean,
    val is_baby: Boolean,
    val is_legendary:Boolean,
    val is_mythical:Boolean,
    val name:String,
    val names: List<PokemonNameLanguageDTO>,
    val order: Int
)

data class DTOPokemonApiGenera(
    val genus: String,
    val language: PokemonNameUrlDTO
)