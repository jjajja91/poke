package scan.util.pokemon

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.dto.PokemonListDTO

object PokemonConst {
    const val POKEMON_API_URL = "https://pokeapi.co/api/v2"
    const val TYPE_COUNT = 18
    val TYPE_ID_RANGE: IntRange = 1..TYPE_COUNT
    val TYPE_ID_SET: Set<Int> = TYPE_ID_RANGE.toSet()
    fun getIdForUrl(url: String): Int? = url.trimEnd('/').substringAfterLast('/').toIntOrNull()
    fun getSubUrl(fullUrl: String) = if (fullUrl.startsWith(POKEMON_API_URL)) fullUrl.removePrefix(POKEMON_API_URL) else fullUrl
}