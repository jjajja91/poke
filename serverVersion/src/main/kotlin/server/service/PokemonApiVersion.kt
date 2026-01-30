package server.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.enum.EnumFailDomain
import scan.enum.EnumLanguage
import server.dto.DTOPokemonApiVersion
import server.dto.DTOVersion

@Component
class PokemonApiVersion(
    private val pokemonWebClient: WebClient
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val domain = EnumFailDomain.VERSION
    suspend fun fetchVersion(id: Int): DTOVersion {
        log.info("[POKE API 요청/${domain.name}] ID:$id")
        val item = pokemonWebClient.get()
            .uri("/${domain.apiKey}/${id}/")
            .retrieve()
            .awaitBody<DTOPokemonApiVersion>()
        return convertToDTO(item)
    }

    private fun convertToDTO(item: DTOPokemonApiVersion): DTOVersion {
        val nameByLang = item.names.associateBy({ it.language.name }, { it.name })
        val dto = DTOVersion(
            versionRowid = item.id,
            nameEn = nameByLang[EnumLanguage.EN.key] ?: item.name,
            nameKr = nameByLang[EnumLanguage.KR.key] ?: item.name,
            nameJp = nameByLang[EnumLanguage.JP.key] ?: nameByLang[EnumLanguage.JP2.key] ?: item.name,
            groupKey = item.version_group.name
        )
        log.info("[POKE API 요청 완료/${domain.name}] ID:$${dto.versionRowid}, 이름: $${dto.nameKr}")
        return dto
    }
}