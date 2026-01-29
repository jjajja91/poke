package server.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.batch.service.SvBatch
import scan.batch.service.SvBatchJobRunner
import server.entity.EntPokemon
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import scan.enum.EnumStat
import scan.sql.toJson
import server.repository.RepoPokemon
import scan.util.coroutine.BatchResult
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonConst
import server.dto.PokemonApiDTO
import server.dto.PokemonApiStatDTO
import server.dto.PokemonDetailsDTO
import server.dto.PokemonSpeciesDTO
import kotlin.random.Random

@Service
class SvPokemon(
    private val pokemonRepository: RepoPokemon,
    private val mapper: ObjectMapper,
    private val pokemonWebClient: WebClient,
    private val svBatch: SvBatch,
    private val svBatchJobRunner: SvBatchJobRunner
) {
    private val DOMAIN = EnumFailDomain.POKEMON
    private val DOMAIN_SPECIES = EnumFailDomain.POKEMON_SPECIES
    @Transactional(readOnly = true)
    fun pickRandomBase(): EntPokemon? {
        val total = pokemonRepository.countBase()
        if (total <= 0) return null

        val offset = Random.nextInt(total.toInt())
        return pokemonRepository.findBaseByOffset(offset)
    }
    fun addAllForce():String {
        return svBatchJobRunner.startBatchJob(
            DOMAIN,
            {
                pokemonRepository.deleteAllInBatch()
                svBatch.deleteAllFail(DOMAIN)
                val idSet = PokemonConst.getIdSet(pokemonWebClient, DOMAIN.apiKey)
                addList(idSet)
            }, { sl ->
                pokemonRepository.saveAll(sl)
            }
        )
    }
    fun addAllCheck():String {
        return svBatchJobRunner.startBatchJob(
            DOMAIN,
            {
                val failList = svBatch.findAllFail(DOMAIN)
                addList(failList.map { it.refId }.toSet())
            }, { sl ->
                pokemonRepository.saveAll(sl)
            }
        )
    }
    private suspend fun addList(idSet:Set<Int>): BatchResult<Int, EntPokemon> {
        return if (idSet.isEmpty()) BatchResult(listOf(), listOf())
        else idSet.retryAwaitAll(
            retry = 3, concurrency = 7, delayMs = 3000
        ) { id ->
            item(id)
        }
    }
    private suspend fun item(id:Int): EntPokemon {
        val pokeDTO = pokemonWebClient.get()
            .uri("/${DOMAIN.apiKey}/$id/")
            .retrieve()
            .awaitBody<PokemonApiDTO>()
        require(pokeDTO.is_default) { "skip pokemon data" }
        require(pokeDTO.types.size == 1 || pokeDTO.types.size == 2) { "type size can 1 or 2" }
        require(pokeDTO.stats.size == 6){ "stat size only can 6" }
        val speciesDTO = pokemonWebClient.get()
            .uri("/${DOMAIN_SPECIES.apiKey}/$id/")
            .retrieve()
            .awaitBody<PokemonSpeciesDTO>()
        val nameByLang = speciesDTO.names.associateBy({ it.language.name }, { it.name })
        val descriptionByLang = speciesDTO.flavor_text_entries.associateBy({ it.language.name }, { it.flavor_text })
        val generaByLang = speciesDTO.genera.associateBy({ it.language.name }, { it.genus })
        val type1 = PokemonConst.getIdForUrl(pokeDTO.types[0].type.url) ?: throw Throwable("unknown type id")
        val type2 = if(pokeDTO.types.size == 2) PokemonConst.getIdForUrl(pokeDTO.types[1].type.url) ?: throw Throwable("unknown type id") else type1
        val stat = statsToDetailStat(pokeDTO.stats)
        val details = PokemonDetailsDTO(
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
        val entity = EntPokemon(
            id = pokeDTO.id,
            baseId = speciesDTO.evolves_from_species?.let{ PokemonConst.getIdForUrl(it.url) } ?: pokeDTO.id,
            type1Id = type1,
            type2Id = type2,
            nameEn = nameByLang[EnumLanguage.EN.key] ?: speciesDTO.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: speciesDTO.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: speciesDTO.name,
            hp = stat.hp,
            atk = stat.atk,
            satk = stat.satk,
            def = stat.def,
            sdef = stat.sdef,
            spd = stat.spd,
            details = mapper.toJson(details)
        )
        println(entity)
        return entity
    }
    private fun statsToDetailStat(list:List<PokemonApiStatDTO>):Stat {
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