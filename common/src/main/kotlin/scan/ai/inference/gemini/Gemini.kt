package scan.ai.inference.gemini

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.toEntityFlux
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOChatRes
import scan.ai.inference.dto.DTOContent
import scan.ai.inference.dto.DTOFileRes
import scan.ai.inference.dto.DTOInferenceConfig
import scan.ai.inference.dto.DTOTool
import scan.ai.message.modal.DTOModalMsg
import scan.ai.message.option.DTOOptionMsg
import scan.ai.message.text.DTOTextMsg
import java.io.File
import java.io.FileInputStream
import kotlin.jvm.java

class Gemini(
    config: DTOInferenceConfig
) : InferenceEngine(config) {

    companion object {
        private val sep = Regex("""\r\n|\n|\r""")

        private fun StringBuilder.flushWordBufTrimEnd(): String? {
            var end = length
            while (end > 0 && this[end - 1].isWhitespace()) end--
            if (end == 0) { setLength(0); return null }
            val s = substring(0, end)
            setLength(0)
            return s
        }
    }

    override suspend fun chatStream(request: DTOChatReq, option: DTOOptionMsg, flushWords: Int): Flow<String> {

        val stopRegex = """"finishReason"\s*:\s*"STOP"""".toRegex()

        val geminiReq = convertRequest(request)
        val reqJson = mapper.writeValueAsString(geminiReq)
        log.debug("[Gemini.stream.request] model={} bytes={}", request.model, reqJson.length)

        val builder = StringBuilder()
        val wordBuf = StringBuilder()
        var prevIsSpace = true
        var wordCount = 0

        return webClient().post()
            .uri("/v1beta/models/${request.model}:streamGenerateContent?alt=sse")
            .headers { h -> option.headers.forEach { (k, v) -> h.set(k, v) } }
            .accept(MediaType.TEXT_EVENT_STREAM)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(reqJson)
            .retrieve()
            .onStatus({ it.is4xxClientError }) { res ->
                res.bodyToMono(String::class.java)
                    .flatMap { Mono.error(RuntimeException("Gemini 4xx: $it")) }
            }
            .onStatus({ it.is5xxServerError }) { res ->
                res.bodyToMono(String::class.java)
                    .flatMap { Mono.error(RuntimeException("Gemini 5xx: $it")) }
            }
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
                    if (!line.startsWith("data:")) continue
                    val payload = line.removePrefix("data:").trim()

                    val content = try {
                        convertStreamResponse(payload)
                    } catch (_: Throwable) {
                        ""
                    }

                    if (content.isBlank()) continue

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

                    if (stopRegex.containsMatchIn(payload)) {
                        if (flushWords > 0 && wordCount > 0) {
                            wordBuf.flushWordBufTrimEnd()?.let(out::add)
                            wordCount = 0
                            prevIsSpace = true
                        }
                    }
                }

                Flux.fromIterable(out)
            }
            .asFlow()
    }

    override suspend fun chat(request: DTOChatReq, option: DTOOptionMsg): DTOChatRes? =
        try {
            val geminiReq = convertRequest(request)
            val reqJson = mapper.writeValueAsString(geminiReq)
            log.debug("[Gemini.chat.request] model={} bytes={}", request.model, reqJson.length)

            val raw = webClient().post()
                .uri("/v1beta/models/${request.model}:generateContent")
                .headers { h -> option.headers.forEach { (k, v) -> h.set(k, v) } }
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(reqJson)
                .retrieve()
                .awaitBody<String>()

            val dto = convertResponse(raw)
            dto
        } catch (e: Exception) {
            log.error("[Gemini.chat.error] {}", e.message, e)
            null
        }

    override suspend fun files(purpose: FilePurpose, file: File): DTOFileRes? =
        try {
            val fileBytes = file.readBytes()
            val mimeType = MimeTypeDetector.detect(file)
            val contentLength = fileBytes.size

            val uploadUrl = webClient().post()
                .uri("https://generativelanguage.googleapis.com/upload/v1beta/files")
                .headers { h -> defaultOption.headers.forEach { (k, v) -> h.set(k, v) } }
                .header("X-Goog-Upload-Protocol", "resumable")
                .header("X-Goog-Upload-Command", "start")
                .header("X-Goog-Upload-Header-Content-Length", contentLength.toString())
                .header("X-Goog-Upload-Header-Content-Type", mimeType)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""{"file":{"display_name":"${file.name}"}}""")
                .exchangeToMono { res ->
                    val headers = res.headers().asHttpHeaders()
                    val location = headers.getFirst("x-goog-upload-url")
                    if (location != null) Mono.just(location)
                    else Mono.error(Exception("[gemini] Upload URL not found. Headers: $headers"))
                }
                .awaitSingle()

            val resultJson = webClient().post()
                .uri(uploadUrl)
                .headers { h -> defaultOption.headers.forEach { (k, v) -> h.set(k, v) } }
                .header("X-Goog-Upload-Command", "upload, finalize")
                .header("X-Goog-Upload-Offset", "0")
                .header(HttpHeaders.CONTENT_LENGTH, contentLength.toString())
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .bodyValue(fileBytes)
                .retrieve()
                .awaitBody<String>()

            val geminiFile = mapper.readValue(resultJson, DTOGeminiFileRes::class.java)
            DTOFileRes(id = geminiFile.file.uri)
        } catch (e: Exception) {
            log.error("[Gemini.files.error] {}", e.message, e)
            null
        }

    private fun convertRequest(request: DTOChatReq): DTOGeminiChatReq {
        var hasSystem = false

        val req = DTOGeminiChatReq()

        request.tools?.let { tools ->
            val functions = tools.filterIsInstance<DTOTool.Function>()
            if (functions.isNotEmpty()) {
                req.tools = arrayListOf(
                    DTOGeminiTool(
                        functionDeclarations = functions.map { it.function }.toCollection(ArrayList())
                    )
                )
            }
        }

        request.messages.forEach { msg ->
            when (msg) {
                is DTOTextMsg -> {
                    if (msg.role == "system") {
                        if (!hasSystem) {
                            req.system_instruction = DTOGeminiInstruction(parts = arrayListOf())
                            hasSystem = true
                        }
                        req.system_instruction!!.parts.add(DTOGeminiPart.DTOGeminiText(text = msg.content))
                    } else {
                        req.contents.add(
                            DTOGeminiReqContents(
                                role = convertRole(msg.role, true),
                                parts = arrayListOf(DTOGeminiPart.DTOGeminiText(text = msg.content))
                            )
                        )
                    }
                }
                is DTOModalMsg -> {
                    if (msg.role == "system") {
                        if (!hasSystem) {
                            req.system_instruction = DTOGeminiInstruction(parts = arrayListOf())
                            hasSystem = true
                        }
                        msg.content.forEach { c ->
                            contentProcess(c) { req.system_instruction!!.parts.add(it) }
                        }
                    } else {
                        val contents = DTOGeminiReqContents(
                            role = convertRole(msg.role, true),
                            parts = arrayListOf()
                        )
                        msg.content.forEach { c -> contentProcess(c) { contents.parts.add(it) } }
                        req.contents.add(contents)
                    }
                }
                else -> {}
            }
        }

        return req
    }

    private fun convertResponse(raw: String): DTOChatRes {
        val res = mapper.readValue(raw, DTOGeminiChatRes::class.java)

        val choices = arrayListOf<DTOChatRes.Choice>()
        res.candidates.forEachIndexed { idx, cand ->
            cand.content.parts.forEach { part ->
                val text = part["text"] ?: ""
                choices.add(
                    DTOChatRes.Choice(
                        index = idx,
                        message = DTOChatRes.Choice.ResMessage(
                            role = convertRole(cand.content.role, false),
                            content = text
                        )
                    )
                )
            }
        }
        return DTOChatRes(choices = choices)
    }

    private fun convertStreamResponse(payload: String): String {
        val res = mapper.readValue(payload, DTOGeminiStreamChatRes::class.java)
        return res.candidates.firstOrNull()
            ?.content?.parts?.firstOrNull()
            ?.get("text")
            .orEmpty()
    }

    private fun convertRole(role: String, isRequest: Boolean): String =
        when (role) {
            "user" -> "user"
            "assistant", "model" -> if (isRequest) "model" else "assistant"
            "system" -> "system"
            else -> throw IllegalArgumentException("[gemini] unknown role: $role")
        }

    private fun contentProcess(content: DTOContent, block: (DTOGeminiPart) -> Unit) {
        when (content) {
            is DTOContent.Text ->
                block(DTOGeminiPart.DTOGeminiText(text = content.text))
            is DTOContent.ImageFile ->
                block(
                    DTOGeminiPart.DTOGeminiFile(
                        file_data = DTOGeminiFileData(
                            file_uri = content.file_id,
                            mime_type = content.file?.let { MimeTypeDetector.detect(it) } ?: "application/octet-stream"
                        )
                    )
                )
            is DTOContent.ImageUrl ->
                block(
                    DTOGeminiPart.DTOGeminiBase64(
                        inline_data = DTOGeminiInlineData(
                            mime_type = content.image_url["url"]?.let { MimeTypeDetector.detectBase64(it).first } ?: "image/png",
                            data = content.image_url["url"]?.let { MimeTypeDetector.detectBase64(it).second } ?: ""
                        )
                    )
                )
            is DTOContent.InputAudio -> {}
            else -> {}
        }
    }
}

object MimeTypeDetector {
    fun detect(file: File, maxBytes: Int = 64): String {
        val header = readFileHeader(file, maxBytes)
        return when {
            header.startsWith(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)) -> "image/png"
            header.startsWith(byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())) -> "image/jpeg"
            header.startsWith("GIF87a".encodeToByteArray()) || header.startsWith("GIF89a".encodeToByteArray()) -> "image/gif"
            header.startsWith("%PDF-".encodeToByteArray()) -> "application/pdf"
            header.startsWith(byteArrayOf(0x50, 0x4B, 0x03, 0x04)) -> "application/zip or openxml"
            header.startsWith(byteArrayOf(0xD0.toByte(), 0xCF.toByte(), 0x11, 0xE0.toByte())) -> "application/msword (old office)"
            header.startsWith(byteArrayOf(0x42, 0x4D)) -> "image/bmp"
            header.startsWith(byteArrayOf(0x1F, 0x8B.toByte())) -> "application/gzip"
            header.startsWith(byteArrayOf(0x52, 0x49, 0x46, 0x46)) && header.contains("WEBP".encodeToByteArray()) -> "image/webp"
            else -> throw Throwable("[gemini] Unknown file format")
        }
    }

    fun detectBase64(base64: String): Pair<String, String> {
        val regex = Regex("^data:([^;]+);base64,(.+)$")
        val match = regex.find(base64)
        return if (match != null && match.groupValues.size == 3) {
            val mimeType = match.groupValues[1]
            val base64Data = match.groupValues[2]
            mimeType to base64Data
        } else throw Throwable("[gemini] key name url -> _url or only base64 format")
    }

    private fun readFileHeader(file: File, maxBytes: Int): ByteArray {
        FileInputStream(file).use { input ->
            val buffer = ByteArray(maxBytes)
            val read = input.read(buffer, 0, maxBytes)
            return if (read < maxBytes) buffer.copyOf(read) else buffer
        }
    }

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
        if (this.size < prefix.size) return false
        return prefix.indices.all { this[it] == prefix[it] }
    }

    private fun ByteArray.contains(sub: ByteArray): Boolean {
        outer@ for (i in 0..this.size - sub.size) {
            for (j in sub.indices) {
                if (this[i + j] != sub[j]) continue@outer
            }
            return true
        }
        return false
    }
}