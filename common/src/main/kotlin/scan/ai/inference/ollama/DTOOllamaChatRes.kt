package scan.ai.inference.ollama

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import scan.ai.inference.dto.DTOChatRes.Choice.ResMessage.FunctionCall

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class DTOOllamaChatRes(
    val model:String? = null,
    val message:DTOOllamaResMessage,
    val done_reason:String? = null,
    val done:Boolean? = null,
    val created_at: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
internal data class DTOOllamaResMessage(
    val role:String,
    val tool_calls:ArrayList<DTOOllamaMessage>? = null,
    val content:String
)

internal data class DTOOllamaCall(val function:FunctionCall)