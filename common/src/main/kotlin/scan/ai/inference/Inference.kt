package scan.ai.inference


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import scan.ai.inference.gemini.Gemini
import scan.ai.inference.ollama.Ollama
import scan.ai.inference.openai.OpenAI
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOChatRes
import scan.ai.inference.dto.DTOContent
import scan.ai.inference.dto.DTOFileRes
import scan.ai.inference.dto.DTOInferenceConfig
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.function.base.ToolRegistry
import scan.ai.message.function.functionResult
import scan.ai.message.modal.DTOModalMsg
import scan.ai.message.option.DTOOptionMsg
import scan.ai.message.text.DTOTextMsg
import scan.ai.message.text.assistant
import scan.util.web.WebClients
import java.io.File

object InferenceFactory {
    fun create(config: DTOInferenceConfig): InferenceEngine =
        when (config.provider) {
            EnumInferenceProvider.OPENAI -> OpenAI(config)
            EnumInferenceProvider.GEMINI -> Gemini(config)
            EnumInferenceProvider.OLLAMA -> Ollama(config)
        }
}

abstract class InferenceEngine(
    protected val config: DTOInferenceConfig
) {
    enum class FilePurpose(val v: String) {
        FINETUNE("fine-tune"),
        RETRIEVAL("retrieval"),
        ASSISTANTS("assistants"),
        VISION("vision")
    }

    protected val log = LoggerFactory.getLogger(javaClass)
    protected val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
    protected val url: String get() = config.url
    val defaultOption: DTOOptionMsg get() = config.defaultOption

    private val _webClient: WebClient by lazy {
        WebClients.create(
            baseUrl = url,
            insecureSsl = config.http.insecureSsl,
            timeout = config.http.timeout,
            maxInMemorySize = config.http.maxInMemorySize
        )
    }
    protected fun webClient(): WebClient = _webClient

    private suspend fun prepareChat(req: DTOChatReq, messages: ArrayList<DTOMessage>): Boolean {
        val user: DTOTextMsg = messages
            .lastOrNull { it is DTOModalMsg || (it is DTOTextMsg && it.role == "user") }
            ?.let {
                if (it is DTOModalMsg) {
                    DTOTextMsg(
                        role = "user",
                        content = it.content
                            .filterIsInstance<DTOContent.Text>()
                            .joinToString("\n") { c -> c.text }
                    )
                } else it as DTOTextMsg
            }
            ?: return false

        req.stream = false
        val option = messages.filterIsInstance<DTOOptionMsg>().let {
            if (it.isEmpty()) defaultOption
            else {
                messages.removeAll(it)
                it.last()
            }
        }

        option.prepare(this, req, user, req.tasks)

        val prepared: List<DTOMessage> = try {
            messages.mapNotNull { m -> m.prepare(this, req, user, req.tasks) }.flatten()
        } catch (e: Throwable) {
            log.error("DTOMessage.prepare failed: {}", e.message, e)
            return false
        }

        req.messages = ArrayList(prepared)
        req.tasks.forEach { it(DTOMessage.Event.PREPARED, this, req) }

        req.userMsg = user
        req.optionMsg = option
        return true
    }

    protected suspend fun updateChat(user: DTOTextMsg, response: DTOTextMsg, messages: ArrayList<DTOMessage>) {
        messages.forEach { it.update(this, user, response) }
    }

    suspend fun chatCompletionFlow(messages: ArrayList<DTOMessage>, flushWords: Int = 0): Flow<String>? {
        val req = DTOChatReq()
        if (!prepareChat(req, messages)) return null
        req.stream = true

        val flow = chatStream(req, req.optionMsg!!, flushWords) ?: return null

        return flow.onEach { chunk ->
            updateChat(req.userMsg!!, DTOMessage.assistant(chunk), messages)
        }
    }

    suspend fun chatCompletion(messages: ArrayList<DTOMessage>, block: ((DTOChatRes) -> Unit)? = null): DTOTextMsg? {
        val req = DTOChatReq()
        if (!prepareChat(req, messages)) return null
        while (true) {
            val result = chat(req, req.optionMsg!!) ?: run {
                req.tasks.forEach { it(DTOMessage.Event.RESPONSE_FAIL, this, req) }
                return null
            }
            val toolCalls = result.choices.firstOrNull()?.message?.tool_calls
            if (toolCalls.isNullOrEmpty()) {
                val content = result.choices.firstOrNull()?.message?.content.orEmpty()
                val response = DTOMessage.assistant(content)

                updateChat(req.userMsg!!, response, messages)
                req.tasks.forEach { it(DTOMessage.Event.RESPONSE_OK, this, req) }
                block?.invoke(result)
                return response
            }
            req.tool_choice = "auto"
            toolCalls.forEach { call ->
                val fn = call.function
                val argsJson = fn.arguments

                val tool = ToolRegistry.get(fn.name)
                val out = tool?.invoke(parseArgs(argsJson))

                if (out != null) {
                    req.messages.add(DTOMessage.functionResult(call.id, fn.name, out))
                }
            }
        }
    }

    protected open suspend fun chatStream(request: DTOChatReq, option: DTOOptionMsg, flushWords: Int): Flow<String>? = null

    protected abstract suspend fun chat(request: DTOChatReq, option: DTOOptionMsg): DTOChatRes?

    abstract suspend fun files(purpose: FilePurpose, file: File): DTOFileRes?

    private fun parseArgs(json: String): Map<String, Any?> {
        return try {
            mapper.readValue(json, Map::class.java) as Map<String, Any?>
        } catch (_: Throwable) {
            emptyMap()
        }
    }
}