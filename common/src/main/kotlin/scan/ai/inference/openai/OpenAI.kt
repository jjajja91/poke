@file:Suppress("NOTHING_TO_INLINE")

package scan.ai.inference.openai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.client.toEntityFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOChatRes
import scan.ai.inference.dto.DTOFileRes
import scan.ai.inference.dto.DTOInferenceConfig
import scan.ai.message.option.DTOOptionMsg
import java.io.File

class OpenAI(
    config: DTOInferenceConfig
) : InferenceEngine(config) {

    companion object {
        private val sep = Regex("""\r\n|\n|\r""")
        private fun StringBuilder.flushWordBufTrimEnd(): String? {
            var end = length
            while (end > 0 && this[end - 1].isWhitespace()) end--
            if (end == 0) {
                setLength(0)
                return null
            }
            val s = substring(0, end)
            setLength(0)
            return s
        }
    }
    override suspend fun chatStream(request: DTOChatReq, option: DTOOptionMsg, flushWords: Int): Flow<String> {
        val requestJson = mapper.writeValueAsString(request)
        val builder = StringBuilder()
        val wordBuf = StringBuilder()
        val result = StringBuilder()
        var prevIsSpace = true
        var wordCount = 0
        return webClient()
            .post()
            .uri("/v1/chat/completions")
            .headers { h -> option.headers.forEach { (k, v) -> h[k] = v } }
            .accept(MediaType.TEXT_EVENT_STREAM)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestJson)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { res ->
                res.bodyToMono<String>()
                    .flatMap { Mono.error(RuntimeException("OpenAI 4xx: $it")) }
            }
            .onStatus({ it.is5xxServerError }) { res ->
                res.bodyToMono<String>()
                    .flatMap { Mono.error(RuntimeException("OpenAI 5xx: $it")) }
            }
            .toEntityFlux<DataBuffer>()
            .flatMapMany { it.body ?: Flux.empty() }
            .publishOn(Schedulers.boundedElastic())
            .map { it.toString(Charsets.UTF_8) }
            .concatMap { chunk ->
                builder.append(chunk)

                val text = builder.toString()
                val parts = text.split(sep)
                val endsWithNl = text.endsWith('\n') || text.endsWith('\r')
                val lines = if (endsWithNl) parts else parts.dropLast(1)
                val tail = if (endsWithNl) "" else parts.lastOrNull().orEmpty()

                builder.setLength(0)
                if (tail.isNotEmpty()) builder.append(tail)

                val out = arrayListOf<String>()

                for (line in lines) {
                    if (!line.startsWith("data:")) continue
                    val payload = line.removePrefix("data:").trim()
                    if (payload == "[DONE]") {
                        if (flushWords > 0 && wordCount > 0) {
                            wordBuf.flushWordBufTrimEnd()?.let(out::add)
                            wordCount = 0
                            prevIsSpace = true
                        }
                        continue
                    }

                    val content = try {
                        val node = mapper.readTree(payload)
                        node["choices"]?.get(0)
                            ?.get("delta")
                            ?.get("content")
                            ?.asText()
                            ?: ""
                    } catch (_: Throwable) {
                        ""
                    }

                    if (content.isBlank()) continue

                    if (flushWords <= 0) {
                        out += content
                    } else {
                        for (ch in content) {
                            val isSpace = ch.isWhitespace()
                            if (prevIsSpace && !isSpace) wordCount++
                            wordBuf.append(ch)

                            val wordEnded = isSpace && !prevIsSpace
                            if (wordEnded && wordCount >= flushWords) {
                                wordBuf.flushWordBufTrimEnd()?.let(out::add)
                                wordCount = 0
                            }
                            prevIsSpace = isSpace
                        }
                    }
                }

                Flux.fromIterable(out)
            }
            .asFlow()
            .onEach { result.append(it) }
            .onCompletion {
                log.debug("OpenAI stream completed, length={}", result.length)
            }
    }

    override suspend fun chat(request: DTOChatReq, option: DTOOptionMsg): DTOChatRes? =
        try {
            val requestJson = mapper.writeValueAsString(request)

            val responseJson = webClient()
                .post()
                .uri("/v1/chat/completions")
                .headers { h -> option.headers.forEach { (k, v) -> h[k] = v } }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestJson)
                .retrieve()
                .bodyToMono<String>()
                .awaitSingle()

            mapper.readValue(responseJson, DTOChatRes::class.java)
        } catch (e: Exception) {
            log.error("OpenAI chat error: {}", e.message, e)
            null
        }

    override suspend fun files(purpose: FilePurpose, file: File): DTOFileRes? =
        try {
            val body = MultipartBodyBuilder().apply {
                part("file", FileSystemResource(file)).filename(file.name)
                part("purpose", purpose.v)
            }.build()

            val json = webClient()
                .post()
                .uri("/v1/files")
                .headers { h -> defaultOption.headers.forEach { (k, v) -> h[k] = v } }
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body))
                .retrieve()
                .bodyToMono<String>()
                .awaitSingle()

            mapper.readValue(json, DTOFileRes::class.java)
        } catch (e: Exception) {
            log.error("OpenAI file upload error: {}", e.message, e)
            null
        }
}