package server.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.dto.PokemonNameUrlDTO
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import scan.util.pokemon.PokemonConst
import server.dto.DTOPokemonApiType
import server.dto.DTOType
import server.dto.DTOTypeRelation
import server.dto.DTOTypeRelationDamage
import server.dto.PokemonDamageRelationDTO

@Component
class PokemonApiType(
    private val pokemonWebClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val domain = EnumFailDomain.TYPE
    suspend fun fetch(id: Int): DTOType {
        require(id in PokemonConst.TYPE_ID_RANGE) { "type id out of range: $id" }
        log.info("[POKE API 요청/${domain.name}] ID:$id")
        val item = pokemonWebClient.get()
            .uri("/${domain.apiKey}/${id}/")
            .retrieve()
            .awaitBody<DTOPokemonApiType>()
        return convertToDTO(item)
    }

    private fun convertToDTO(item: DTOPokemonApiType): DTOType {
        val relationMap = PokemonConst.TYPE_ID_RANGE
            .associateWith { DTOTypeRelationDamage(1.0, 1.0) }
            .toMutableMap()
        applyDamageRelations(relationMap, item.damage_relations)
        val nameByLang = item.names.associateBy({ it.language.name.lowercase() }, { it.name })
        val dto = DTOType(
            typeRowid = item.id,
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key]
                ?: nameByLang[EnumLanguage.JP2.key]
                ?: item.name,
            relation = DTOTypeRelation(relationMap)
        )
        log.info("[POKE API 요청 완료/${domain.name}] ID:${dto.typeRowid}, 이름:${dto.nameKr}")
        return dto
    }

    private fun applyDamageRelations(relationMap: MutableMap<Int, DTOTypeRelationDamage>, damageRelations: PokemonDamageRelationDTO) {
        applyTo(relationMap, damageRelations.double_damage_to, 2.0) { dto, v ->
            dto.damageTo = v
        }
        applyTo(relationMap, damageRelations.double_damage_from, 2.0) { dto, v ->
            dto.damageFrom = v
        }
        applyTo(relationMap, damageRelations.half_damage_to, 0.5) { dto, v ->
            dto.damageTo = v
        }
        applyTo(relationMap, damageRelations.half_damage_from, 0.5) { dto, v ->
            dto.damageFrom = v
        }
        applyTo(relationMap, damageRelations.no_damage_to, 0.0) { dto, v ->
            dto.damageTo = v
        }
        applyTo(relationMap, damageRelations.no_damage_from, 0.0) { dto, v ->
            dto.damageFrom = v
        }
    }

    private fun applyTo(
        relationMap: MutableMap<Int, DTOTypeRelationDamage>,
        urls: List<PokemonNameUrlDTO>,
        value: Double,
        setter: (DTOTypeRelationDamage, Double) -> Unit
    ) {
        for (u in urls) {
            val id = PokemonConst.getIdForUrl(u.url) ?: continue
            val dto = relationMap[id] ?: continue
            setter(dto, value)
        }
    }
}