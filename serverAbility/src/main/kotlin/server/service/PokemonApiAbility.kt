package server.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import server.dto.DTOAbility
import server.dto.DTOPokemonApiAbility

@Component
class PokemonApiAbility(
    private val pokemonWebClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val domain = EnumFailDomain.ABILITY
    suspend fun fetch(id: Int): DTOAbility {
        log.info("[POKE API 요청/${domain.name}] ID:$id")
        val item = pokemonWebClient.get()
            .uri("/${domain.apiKey}/${id}/")
            .retrieve()
            .awaitBody<DTOPokemonApiAbility>()
        return convertToDTO(item)
    }

    private fun convertToDTO(item: DTOPokemonApiAbility): DTOAbility {
        val nameByLang = item.names.associateBy({ it.language.name }, { it.name })
        val descriptionByLang = item.flavor_text_entries.associateBy({ it.language.name }, { it.flavor_text })
        val dto = DTOAbility(
            abilityRowid = item.id,
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: item.name,
            descriptionEn = descriptionByLang[EnumLanguage.EN.key] ?: "",
            descriptionKr = descriptionByLang[EnumLanguage.KR.key] ?: "",
            descriptionJp = descriptionByLang[EnumLanguage.JP.key] ?: descriptionByLang[EnumLanguage.JP2.key] ?: "",
        )
        log.info("[POKE API 요청 완료/${domain.name}] ID:${dto.abilityRowid}, 이름:${dto.nameKr}")
        return dto
    }
}