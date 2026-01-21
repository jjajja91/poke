package server.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import scan.util.pokemon.PokemonConst
import scan.util.web.WebClients
import java.time.Duration

@Configuration
class PokemonApiConfig(
    private val props: PokemonApiProps
) {
    @Bean
    fun pokemonWebClient(): WebClient =
        WebClients.create(
            baseUrl = props.baseUrl,
            insecureSsl = props.insecureSsl,
            timeout = props.timeout,
            maxInMemorySize = props.maxInMemorySizeMb * 1024 * 1024
        )
}

@ConfigurationProperties(prefix = "pokemon.api")
data class PokemonApiProps(
    val baseUrl: String = PokemonConst.POKEMON_API_URL,
    val insecureSsl: Boolean = false,
    val timeout: Duration = Duration.ofSeconds(15),
    val maxInMemorySizeMb: Int = 5
)