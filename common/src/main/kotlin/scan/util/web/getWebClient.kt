package scan.util.web

import io.netty.channel.ChannelOption
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.client.toEntityFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.netty.http.client.HttpClient
import java.io.File
import java.io.FileOutputStream
import java.time.Duration
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.createTempFile

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
        baseUrl: String = "",
        headers: Map<String, String> = emptyMap(),
        insecureSsl: Boolean = false,
        timeout: Duration = Duration.ofSeconds(10),
        maxInMemorySize: Int = 10 * 1024 * 1024
    ): WebClient {
        val strategies = ExchangeStrategies.builder()
            .codecs { it.defaultCodecs().maxInMemorySize(maxInMemorySize) }
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
        ExchangeFilterFunction.ofRequestProcessor { Mono.just(it) }

    private fun logResponse(): ExchangeFilterFunction =
        ExchangeFilterFunction.ofResponseProcessor { Mono.just(it) }
}

suspend fun download(url: String): Pair<ByteArray, MediaType?> =
    WebClients.create(maxInMemorySize = 20 * 1024 * 1024)
        .get()
        .uri(url)
        .exchangeToMono { res ->
            res.bodyToMono<ByteArray>()
                .map { it to res.headers().contentType().orElse(null) }
        }
        .awaitSingle()

suspend fun downloadToFile(url: String): Pair<File, MediaType?> {
    val tempFile = createTempFile("download-", ".tmp").toFile()
    tempFile.deleteOnExit()

    val contentTypeRef = AtomicReference<MediaType?>()

    try {
        FileOutputStream(tempFile).use { out ->
            WebClients.create()
                .get()
                .uri(url)
                .retrieve()
                .toEntityFlux<DataBuffer>()
                .doOnNext { contentTypeRef.set(it.headers.contentType) }
                .flatMapMany { it.body ?: Flux.empty() }
                .publishOn(Schedulers.boundedElastic())
                .doOnNext { buf ->
                    try {
                        val bytes = ByteArray(buf.readableByteCount())
                        buf.read(bytes)
                        out.write(bytes)
                    } finally {
                        DataBufferUtils.release(buf)
                    }
                }
                .collectList()
                .awaitSingle()
        }
        return tempFile to contentTypeRef.get()
    } catch (e: Exception) {
        tempFile.delete()
        throw e
    }
}

fun Pair<ByteArray, MediaType?>.toBase64(): String =
    "data:${second ?: MediaType.APPLICATION_OCTET_STREAM};base64," +
            Base64.getEncoder().encodeToString(first)

fun Pair<File, MediaType?>.toBase64(delete: Boolean = true): String {
    val bytes = first.readBytes()
    if (delete) first.delete()
    return "data:${second ?: MediaType.APPLICATION_OCTET_STREAM};base64," +
            Base64.getEncoder().encodeToString(bytes)
}

fun MediaType.getExt(): String = when (toString().lowercase()) {
    "image/jpeg" -> ".jpg"
    "image/png" -> ".png"
    "image/gif" -> ".gif"
    "image/webp" -> ".webp"
    "application/pdf" -> ".pdf"
    else -> ".bin"
}