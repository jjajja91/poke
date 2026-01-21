package server.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.batch.SvBatch
import server.entity.EntPokemon
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import scan.enum.EnumStat
import server.repository.RepoPokemon
import scan.util.coroutine.AppScope
import scan.util.coroutine.JobStatusStore
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonConst
import server.dto.PokemonApiDTO
import server.dto.PokemonApiStatDTO
import server.dto.PokemonDetailsDTO
import server.dto.PokemonSpeciesDTO
import server.dto.toPokemonDetailsJson
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

@Service
class SvPokemon(
    private val pokemonRepository: RepoPokemon,
    private val mapper: ObjectMapper,
    private val pokemonWebClient: WebClient,
    private val svBatch: SvBatch,
    private val appScope: AppScope,
    private val statusStore: JobStatusStore
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
    // force 저장 진입점 (요청만 하고 바로 응답 / 백그라운드에서 진행)
    fun startAddAllForce():String {
        val jobId = UUID.randomUUID().toString()
        statusStore.start(jobId, name = "${DOMAIN.name}:addAllForce")
        appScope.launchApp("${DOMAIN.name}:addAllForce$jobId") {
            runAddAllForce(jobId)
        }
        return jobId
    }

    private suspend fun runAddAllForce(jobId: String) {
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        try {
            // 1) 기존 데이터/실패 기록 정리 (DB 작업이므로 IO)
            withContext(Dispatchers.IO) {
                pokemonRepository.deleteAllInBatch()
                svBatch.deleteAllFail(DOMAIN)
            }

            // 2) 전체 idSet 가져오기 (네트워크 suspend)
            val idSet = PokemonConst.getIdSet(pokemonWebClient, DOMAIN.apiKey)
            statusStore.update(jobId, total = idSet.size)
            if (idSet.isEmpty()) {
                statusStore.done(jobId, success = 0, fail = 0)
                return
            }
            // 3) 병렬 수집
            val result = idSet.retryAwaitAll(
                retry = 3, concurrency = 7, delayMs = 3000
            ) { id ->
                item(id)
            }
            // 간단 집계
            successCount.set(result.successList.size)
            failCount.set(result.failList.size)
            // 4) DB 저장
            withContext(Dispatchers.IO) {
                svBatch.batchAll(DOMAIN, result) { sl ->
                    pokemonRepository.saveAll(sl)
                }
            }
            statusStore.done(jobId, success = successCount.get(), fail = failCount.get())
        } catch (t: Throwable) {
            statusStore.failed(jobId, success = successCount.get(), fail = failCount.get(), message = t.message)
            throw t
        }
    }
    suspend fun addAllCheck() {
        val failList = svBatch.findAllFail(DOMAIN)
        addList(failList.map { it.refId }.toSet())
    }
    private suspend fun addList(idSet:Set<Int>) {
        if (idSet.isEmpty()) return
        val result = idSet.retryAwaitAll(
            retry = 3, concurrency = 7, delayMs = 3000
        ) { id ->
            item(id)
        }
        withContext(Dispatchers.IO) {
            svBatch.batchAll(DOMAIN, result) { sl ->
                pokemonRepository.saveAll(sl)
            }
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
            details = mapper.toPokemonDetailsJson(details)
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