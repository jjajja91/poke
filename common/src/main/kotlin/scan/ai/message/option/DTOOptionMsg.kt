package scan.ai.message.option

import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.text.DTOTextMsg

data class DTOOptionMsg(
    override val role: String? = null,
    var llmModel: String? = null,
    var temperature: Double = -100.0,
    var top_p: Double = -100.0,
    var max_tokens: Int = -100,
    var headers: MutableMap<String, String> = hashMapOf(),
) : DTOMessage {

    fun addHeader(key: String, value: String): DTOOptionMsg {
        headers[key] = value
        return this
    }

    fun addOpenAIKey(apiKey: String): DTOOptionMsg {
        headers["Authorization"] = "Bearer $apiKey"
        return this
    }

    override suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<DTOMessage.Task>): List<DTOMessage> {
        val d = inference.defaultOption
        if (llmModel.isNullOrBlank()) llmModel = d.llmModel
        if (temperature == -100.0 && d.temperature != -100.0) temperature = d.temperature
        if (top_p == -100.0 && d.top_p != -100.0) top_p = d.top_p
        if (max_tokens == -100 && d.max_tokens != -100) max_tokens = d.max_tokens

        d.headers.forEach { (k, v) ->
            if (headers[k] == null) headers[k] = v
        }

        llmModel?.takeIf { it.isNotBlank() }?.let { request.model = it }

        val m = llmModel ?: ""
        if (m.startsWith("gpt-5")) {
            temperature = -100.0
            top_p = -100.0
        }

        if (temperature != -100.0) request.temperature = temperature
        if (top_p != -100.0) request.top_p = top_p
        if (max_tokens != -100) request.max_tokens = max_tokens

        return listOf(this)
    }

    override suspend fun update(inference: InferenceEngine, request: DTOTextMsg, response: DTOTextMsg) {}
}