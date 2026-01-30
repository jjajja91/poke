package server.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import scan.util.pokemon.PokemonConst
import server.dto.DTOMove
import server.dto.DTOMoveDetail
import server.dto.DTOPokemonApiMove

@Component
class PokemonApiMove(
    private val pokemonWebClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val domain = EnumFailDomain.MOVE
    suspend fun fetch(id: Int): DTOMove {
        log.info("[POKE API 요청/${domain.name}] ID:$id")
        val item = pokemonWebClient.get()
            .uri("/${domain.apiKey}/${id}/")
            .retrieve()
            .awaitBody<DTOPokemonApiMove>()
        return convertToDTO(item)
    }

    private fun convertToDTO(item: DTOPokemonApiMove): DTOMove {
        val nameByLang = item.names.associateBy({ it.language.name }, { it.name })
        val descriptionByLang = item.flavor_text_entries.associateBy({ it.language.name }, { it.flavor_text })
        val detail = DTOMoveDetail(
            accuracy = item.accuracy,
            power = item.power,
            pp = item.pp,
            damageClass = PokemonConst.getIdForUrl(item.damage_class.url) ?: throw Throwable("unknown damage_class id")
        )
        val dto = DTOMove(
            moveRowid = item.id,
            typeRowid = PokemonConst.getIdForUrl(item.type.url) ?: throw Throwable("unknown type id"),
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: item.name,
            descriptionEn = descriptionByLang[EnumLanguage.EN.key] ?: "",
            descriptionKr = descriptionByLang[EnumLanguage.KR.key] ?: "",
            descriptionJp = descriptionByLang[EnumLanguage.JP.key] ?: descriptionByLang[EnumLanguage.JP2.key] ?: "",
            details = detail
        )
        log.info("[POKE API 요청 완료/${domain.name}] ID:${dto.moveRowid}, 이름:${dto.nameKr}")
        return dto
    }
}