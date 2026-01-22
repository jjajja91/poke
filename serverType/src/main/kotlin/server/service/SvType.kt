package server.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.batch.SvBatch
import server.entity.EntType
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import server.repository.RepoType
import scan.util.coroutine.retryAwaitAll
import scan.util.pokemon.PokemonConst
import scan.dto.PokemonNameUrlDTO
import scan.util.pokemon.toJson
import server.dto.PokemonTypeDTO
import server.dto.PokemonTypeRelationDTO

@Service
class SvType(
    private val typeRepository: RepoType,
    private val mapper: ObjectMapper,
    private val pokemonWebClient: WebClient,
    private val svBatch: SvBatch
) {
    private val DOMAIN = EnumFailDomain.TYPE
    fun findAll():List<EntType> {
        return typeRepository.findAll()
    }
    suspend fun addAllForce() {
        svBatch.deleteAllFail(DOMAIN)
        deleteAll()
        addList(PokemonConst.TYPE_ID_SET)
    }
    suspend fun addAllCheck() {
        val failList = svBatch.findAllFail(DOMAIN)
        addList(failList.map { it.refId }.toSet())
    }
    fun deleteAll() {
        typeRepository.deleteAllInBatch()
    }
    private suspend fun addList(idSet:Set<Int>) {
        if (idSet.isEmpty()) return
        val result = idSet.retryAwaitAll(
            retry = 3, concurrency = 6, delayMs = 3000
        ) { id ->
            item(id)
        }
        svBatch.batchAll(DOMAIN, result) {
            typeRepository.saveAll(it)
        }
    }
    private suspend fun item(id: Int): EntType {
        require(id in PokemonConst.TYPE_ID_RANGE) { "type id out of range: $id" }
        val item = pokemonWebClient.get()
            .uri("/${DOMAIN.apiKey}/${id}/")
            .retrieve()
            .awaitBody<PokemonTypeDTO>()
        val relationMap = PokemonConst.TYPE_ID_RANGE.associateWith { PokemonTypeRelationDTO(1.0, 1.0) }.toMutableMap()
        applyTo(relationMap, item.damage_relations.double_damage_to, 2.0) { dto, v -> dto.damageTo = v }
        applyTo(relationMap, item.damage_relations.double_damage_from, 2.0) { dto, v -> dto.damageFrom = v }
        applyTo(relationMap, item.damage_relations.half_damage_to,     0.5) { dto, v -> dto.damageTo = v }
        applyTo(relationMap, item.damage_relations.half_damage_from,   0.5) { dto, v -> dto.damageFrom = v }
        applyTo(relationMap, item.damage_relations.no_damage_to,       0.0) { dto, v -> dto.damageTo = v }
        applyTo(relationMap, item.damage_relations.no_damage_from,     0.0) { dto, v -> dto.damageFrom = v }
        val nameByLang = item.names.associateBy({ it.language.name }, { it.name })
        val entity = EntType(
            id = item.id,
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: item.name,
            contents = mapper.toJson(relationMap)
        )
        println(entity)
        return entity
    }
    // 맵 순회해서 데미지 반영하는 함수
    private fun applyTo(
        relationMap: MutableMap<Int, PokemonTypeRelationDTO>,
        urls: List<PokemonNameUrlDTO>,
        value: Double,
        setter: (PokemonTypeRelationDTO, Double) -> Unit
    ) {
        for (u in urls) {
            val id = PokemonConst.getIdForUrl(u.url) ?: continue
            val dto = relationMap[id] ?: continue
            setter(dto, value)
        }
    }
}