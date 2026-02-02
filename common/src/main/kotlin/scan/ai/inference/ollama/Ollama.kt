package scan.ai.inference.ollama

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.client.toEntityFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import scan.ai.inference.InferenceEngine
import scan.ai.inference.gemini.MimeTypeDetector
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOChatRes
import scan.ai.inference.dto.DTOContent
import scan.ai.inference.dto.DTOFileRes
import scan.ai.inference.dto.DTOInferenceConfig
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.modal.DTOModalMsg
import scan.ai.message.option.DTOOptionMsg
import scan.ai.message.text.DTOTextMsg
import java.io.File

class Ollama(
    config: DTOInferenceConfig
) : InferenceEngine(config) {

    companion object {
        private val sep = Regex("""\r\n|\n|\r""")

        private fun sanitized(requestBody: String): String =
            requestBody.replace(
                Regex(""""images"\s*:\s*\[.*?\]""", RegexOption.DOT_MATCHES_ALL),
                """"images":[]"""
            )

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

        val ollamaReq = convertRequest(request, stream = true)
        val reqJson = mapper.writeValueAsString(ollamaReq)

        log.debug("[Ollama.stream.request] {}", sanitized(reqJson))

        val builder = StringBuilder()
        val wordBuf = StringBuilder()
        var prevIsSpace = true
        var wordCount = 0

        return webClient().post()
            .uri("/api/chat")
            .headers { h -> option.headers.forEach { (k, v) -> h.set(k, v) } }
            .accept(MediaType.TEXT_EVENT_STREAM)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(reqJson)
            .retrieve()
            .toEntityFlux<DataBuffer>()
            .flatMapMany { it.body ?: Flux.empty() }
            .publishOn(Schedulers.boundedElastic())
            .map { db ->
                try {
                    db.toString(Charsets.UTF_8)
                } finally {
                    DataBufferUtils.release(db)
                }
            }
            .concatMap { chunk ->
                builder.append(chunk)

                val s = builder.toString()
                val parts = s.split(sep)
                val endsWithNewline = s.endsWith('\n') || s.endsWith('\r')
                val completeLines = if (endsWithNewline) parts else parts.dropLast(1)
                val lastFragment = if (endsWithNewline) "" else parts.lastOrNull().orEmpty()

                builder.setLength(0)
                if (lastFragment.isNotEmpty()) builder.append(lastFragment)

                val out = arrayListOf<String>()

                for (line in completeLines) {
                    if (line.isBlank()) continue

                    val res = try {
                        mapper.readValue(line, DTOOllamaChatRes::class.java)
                    } catch (e: Exception) {
                        log.warn("[Ollama.stream] json parse failed: {}", e.message)
                        continue
                    }

                    val content = res.message.content

                    if (flushWords <= 0) {
                        out += content
                    } else {
                        var i = 0
                        while (i < content.length) {
                            val ch = content[i]
                            val isSpace = ch.isWhitespace()
                            if (prevIsSpace && !isSpace) wordCount++
                            wordBuf.append(ch)
                            val wordJustEnded = isSpace && !prevIsSpace
                            if (wordJustEnded && wordCount >= flushWords) {
                                wordBuf.flushWordBufTrimEnd()?.let(out::add)
                                wordCount = 0
                            }
                            prevIsSpace = isSpace
                            i++
                        }
                    }

                    if (res.done == true && flushWords > 0 && wordCount > 0) {
                        wordBuf.flushWordBufTrimEnd()?.let(out::add)
                        wordCount = 0
                        prevIsSpace = true
                    }
                }

                Flux.fromIterable(out)
            }
            .asFlow()
    }

    override suspend fun chat(request: DTOChatReq, option: DTOOptionMsg): DTOChatRes? =
        try {
            val ollamaReq = convertRequest(request)
            val reqJson = mapper.writeValueAsString(ollamaReq)

            log.debug("[Ollama.chat.request] {}", sanitized(reqJson))

            val raw = webClient().post()
                .uri("/api/chat")
                .headers { h -> option.headers.forEach { (k, v) -> h.set(k, v) } }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reqJson)
                .retrieve()
                .onStatus({ it.isError }) { res ->
                    res.bodyToMono<String>().flatMap { body ->
                        log.error("[Ollama 400] status={} body={}", res.statusCode(), body)
                        Mono.error(RuntimeException("Ollama error ${res.statusCode()}: $body"))
                    }
                }
                .awaitBody<String>()

            convertResponse(raw)
        } catch (e: Exception) {
            log.error("[Ollama.chat.error] {}", e.message, e)
            null
        }

    override suspend fun files(purpose: FilePurpose, file: File): DTOFileRes? {
        log.warn("[Ollama.files] unsupported")
        return null
    }

    private fun convertRequest(
        request: DTOChatReq,
        stream: Boolean = false
    ): DTOOllamaChatReq =
        DTOOllamaChatReq(
            model = request.model,
            stream = stream,
            messages = buildMessages(request.messages)
        )

    private fun buildMessages(
        messages: List<DTOMessage>
    ): ArrayList<DTOOllamaMessage> {

        val result = arrayListOf<DTOOllamaMessage>()

        messages.forEach { msg ->
            when (msg) {
                is DTOTextMsg ->
                    result += DTOOllamaMessage(
                        role = msg.role,
                        content = msg.content
                    )
                is DTOModalMsg ->
                    result += modalToMessages(msg.role, msg.content)

                else -> {}
            }
        }
        return result
    }

    private fun modalToMessages(
        role: String,
        contents: List<DTOContent>
    ): List<DTOOllamaMessage> {

        val result = arrayListOf<DTOOllamaMessage>()
        var isImage = false

        contents.forEach { content ->
            when (content) {
                is DTOContent.Text -> {
                    if (isImage) {
                        result.last().content = content.text
                    } else {
                        result += DTOOllamaMessage(
                            role = role,
                            content = content.text
                        )
                    }
                    isImage = false
                }

                is DTOContent.ImageUrl -> {
                    val (_, base64) = MimeTypeDetector.detectBase64(content.image_url["url"]!!)
                    if (isImage) {
                        result.last().images?.add(base64)
                    } else {
                        result += DTOOllamaMessage(
                            role = role,
                            content = "",
                            images = arrayListOf(base64)
                        )
                    }
                    isImage = true
                }

                else -> throw IllegalStateException("[ollama] unsupported content")
            }
        }
        return result
    }

    private fun convertResponse(raw: String): DTOChatRes {
        val res = mapper.readValue(raw, DTOOllamaChatRes::class.java)
        return DTOChatRes(
            choices = listOf(
                DTOChatRes.Choice(
                    index = 0,
                    message = DTOChatRes.Choice.ResMessage(
                        role = res.message.role,
                        content = res.message.content
                    ),
                    finish_reason = res.done_reason
                )
            )
        )
    }
}