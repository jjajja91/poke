package scan.ai.message.function.base

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody
import scan.ai.inference.dto.DTOTool
import scan.ai.message.function.ToolFunction
import scan.util.web.WebClients
import org.jsoup.Jsoup

object Fetch : ToolFunction {
    override val name: String = "fetch"
    override val description: String =
        "Purpose: Retrieve the main readable text content from a given web page. " +
                "Trigger: Used when the user asks to read, summarize, analyze, or extract information from a specific URL or website."

    override val function: DTOTool.Function =
        DTOTool.Function(
            DTOTool.Function.F(
                name = name,
                description = description
            )
        )
            .paramString("url", "url for search", true)
            .paramString("method", "Optional HTTP method such as GET, POST, PUT, DELETE. Defaults to GET.", false)
            .paramString("jsonbody", "Optional request json body for POST/PUT methods", false)

    data class Req(
        val url: String = "",
        val method: String? = null,
        val jsonbody: String? = null
    )

    private val mapper: ObjectMapper = jacksonObjectMapper()

    override suspend fun invoke(params: Map<String, Any?>): String? {
        val req = runCatching { mapper.convertValue(params, Req::class.java) }
            .getOrElse {
                println("[Fetch] Request parse error: ${it.message} map=${params.keys}")
                return null
            }

        if (req.url.isBlank()) {
            println("[Fetch] Missing url param. map=${params.keys}")
            return null
        }

        val method = (req.method ?: "get").trim().lowercase()

        return try {
            val client = WebClients.create(
                baseUrl = "",
                insecureSsl = true,
                timeout = java.time.Duration.ofSeconds(30),
                maxInMemorySize = 20 * 1024 * 1024
            )

            val response = when (method) {
                "post" -> client.post()
                    .uri(req.url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req.jsonbody ?: "{}")
                "put" -> client.put()
                    .uri(req.url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(req.jsonbody ?: "{}")
                "delete" -> client.delete().uri(req.url)
                else -> client.get().uri(req.url)
            }.retrieve()

            val body = response.awaitBody<String>()

            if (method != "get") return body
            if (!looksLikeHtml(body)) return body

            extractReadableText(body, limit = 5000)
        } catch (e: Exception) {
            println("[Fetch] Response error: ${e.message}")
            null
        }
    }

    private fun looksLikeHtml(body: String): Boolean {
        val s = body.trimStart()
        return s.startsWith("<!doctype", ignoreCase = true) ||
                s.startsWith("<html", ignoreCase = true) ||
                s.contains("<body", ignoreCase = true)
    }

    private fun extractReadableText(html: String, limit: Int): String {
        val doc = Jsoup.parse(html)

        doc.select("script,style,header,footer,noscript,link,meta").remove()

        val texts = doc.select("body *")
            .mapNotNull { el ->
                val t = el.text().trim()
                if (t.length > 10) t else null
            }
            .joinToString(",") { t ->
                t.replace("\n", " ")
                    .replace("\t", " ")
                    .replace("  ", " ")
            }

        return if (texts.length <= limit) texts else texts.substring(0, limit - 3) + "..."
    }
}