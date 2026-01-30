package scan.util.pokemon

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import scan.dto.PokemonListDTO

@Component
class PokemonApiCatalog(
    private val pokemonWebClient: WebClient
) {
    suspend fun fetchIdSet(resource:String): Set<Int> {
        var uri:String? = "/$resource?offset=0&limit=20"
        val idSet = hashSetOf<Int>()
        while(uri != null) {
            val list = runCatching {
                pokemonWebClient.get().uri(uri).retrieve().awaitBody<PokemonListDTO>()
            }.getOrElse { e ->
                println("[ERROR] getIdSet($resource) uri=$uri message=${e.message}")
                return idSet
            }
            uri = list.next?.let { PokemonConst.getSubUrl(it) }
            idSet.addAll(list.results.mapNotNull { PokemonConst.getIdForUrl(it.url) })
        }
        return idSet
    }
}