package scan.util.web

import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration

object WebClients {
    private fun httpClient(
        insecureSsl: Boolean,
        timeout: Duration,
    ): HttpClient {
        var client = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout.toMillis().toInt())
            .responseTimeout(timeout)

        if (insecureSsl) {
            client = client.secure { spec ->
                val ctx = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build()
                spec.sslContext(ctx)
            }
        }
        return client
    }
    fun create(
        baseUrl: String,
        headers: Map<String, String> = emptyMap(),
        insecureSsl: Boolean = false,
        timeout: Duration = Duration.ofSeconds(10),
        maxInMemorySize: Int
    ): WebClient {
        val strategies = ExchangeStrategies.builder()
            .codecs {
                it.defaultCodecs().maxInMemorySize(maxInMemorySize)
            }
            .build()
        val connector = ReactorClientHttpConnector(httpClient(insecureSsl, timeout))
        return WebClient.builder()
            .baseUrl(baseUrl)
            .exchangeStrategies(strategies)
            .clientConnector(connector)
            .defaultHeaders { h -> headers.forEach { (k, v) -> h.set(k, v) } }
            .filter(logRequest())
            .filter(logResponse())
            .build()
    }
    private fun logRequest(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofRequestProcessor { req ->
            Mono.just(req)
        }

    private fun logResponse(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofResponseProcessor { res ->
            Mono.just(res)
        }
}