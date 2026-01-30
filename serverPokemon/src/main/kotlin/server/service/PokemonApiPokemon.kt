package server.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import scan.enum.EnumStat
import scan.util.pokemon.PokemonConst
import server.dto.DTOPokemon
import server.dto.DTOPokemonApiPokemon
import server.dto.DTOPokemonApiSpecies
import server.dto.DTOPokemonApiStat
import server.dto.DTOPokemonDetails
import kotlin.collections.forEach

@Component
class PokemonApiPokemon(
    private val pokemonWebClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val domain = EnumFailDomain.POKEMON
    private val domainSpecies = EnumFailDomain.POKEMON_SPECIES
    suspend fun fetch(id: Int): DTOPokemon {
        log.info("[POKE API 요청/${domain.name}] ID:$id")
        val item = pokemonWebClient.get()
            .uri("/${domain.apiKey}/${id}/")
            .retrieve()
            .awaitBody<DTOPokemonApiPokemon>()
        return convertToDTO(item)
    }

    private suspend fun fetchDetail(id: Int): DTOPokemonApiSpecies {
        return pokemonWebClient.get()
            .uri("/${domainSpecies.apiKey}/$id/")
            .retrieve()
            .awaitBody<DTOPokemonApiSpecies>()
    }

    private suspend fun convertToDTO(pokeDTO: DTOPokemonApiPokemon): DTOPokemon {
        require(pokeDTO.is_default) { "skip pokemon data" }
        require(pokeDTO.types.size == 1 || pokeDTO.types.size == 2) { "type size can 1 or 2" }
        require(pokeDTO.stats.size == 6){ "stat size only can 6" }
        val speciesDTO = fetchDetail(pokeDTO.id)
        val nameByLang = speciesDTO.names.associateBy({ it.language.name }, { it.name })
        val descriptionByLang = speciesDTO.flavor_text_entries.associateBy({ it.language.name }, { it.flavor_text })
        val generaByLang = speciesDTO.genera.associateBy({ it.language.name }, { it.genus })
        val type1 = PokemonConst.getIdForUrl(pokeDTO.types[0].type.url) ?: throw Throwable("unknown type id")
        val type2 = if(pokeDTO.types.size == 2) PokemonConst.getIdForUrl(pokeDTO.types[1].type.url) ?: throw Throwable("unknown type id") else type1
        val stat = statsToDetailStat(pokeDTO.stats)
        val details = DTOPokemonDetails(
            abilityRowids = pokeDTO.abilities.mapNotNull { PokemonConst.getIdForUrl(it.ability.url) },
            isDefault = pokeDTO.is_default,
            moveRowids = pokeDTO.moves.mapNotNull { PokemonConst.getIdForUrl(it.move.url) },
            order = pokeDTO.order,
            sprites = pokeDTO.sprites,
            eggGroupIds = speciesDTO.egg_groups.mapNotNull { PokemonConst.getIdForUrl(it.url) },
            descriptionEn = (descriptionByLang[EnumLanguage.EN.key] ?: ""),
            descriptionKr = (descriptionByLang[EnumLanguage.KR.key] ?: ""),
            descriptionJp = (descriptionByLang[EnumLanguage.JP.key] ?: descriptionByLang[EnumLanguage.JP2.key] ?: ""),
            formSwitchable = speciesDTO.forms_switchable,
            forms = pokeDTO.forms.map { it.name },
            generaEn = (generaByLang[EnumLanguage.EN.key] ?: ""),
            generaKr = (generaByLang[EnumLanguage.KR.key] ?: ""),
            generaJp = (generaByLang[EnumLanguage.JP.key] ?: generaByLang[EnumLanguage.JP2.key] ?: ""),
            hasGenderDifferences = speciesDTO.has_gender_differences,
            isBaby = speciesDTO.is_baby,
            isLegendary = speciesDTO.is_legendary,
            isMythical = speciesDTO.is_mythical,
        )
        val dto = DTOPokemon(
            pokemonRowid = pokeDTO.id,
            type1Rowid = type1,
            type2Rowid = type2,
            baseRowid = speciesDTO.evolves_from_species?.let{ PokemonConst.getIdForUrl(it.url) } ?: pokeDTO.id,
            nameEn = nameByLang[EnumLanguage.EN.key] ?: speciesDTO.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: speciesDTO.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: speciesDTO.name,
            hp = stat.hp,
            atk = stat.atk,
            satk = stat.satk,
            def = stat.def,
            sdef = stat.sdef,
            spd = stat.spd,
            details = details
        )
        log.info("[POKE API 요청 완료/${domain.name}] ID:${dto.pokemonRowid}, 이름:${dto.nameKr}")
        return dto
    }
    private fun statsToDetailStat(list:List<DTOPokemonApiStat>):Stat {
        val stat = Stat()
        list.forEach { dto ->
            val id = PokemonConst.getIdForUrl(dto.stat.url) ?: throw Throwable("unknown stat url ${dto.stat.url}")
            when(EnumStat(id)) {
                EnumStat.HP -> stat.hp = dto.base_stat
                EnumStat.ATK -> stat.atk = dto.base_stat
                EnumStat.DEF -> stat.def = dto.base_stat
                EnumStat.SPD -> stat.spd = dto.base_stat
                EnumStat.SATK -> stat.satk = dto.base_stat
                EnumStat.SDEF -> stat.sdef = dto.base_stat
            }
        }
        return stat
    }
    private data class Stat(
        var hp: Int = 0,
        var atk: Int = 0,
        var satk: Int = 0,
        var spd: Int = 0,
        var def: Int = 0,
        var sdef: Int = 0,
    )
}