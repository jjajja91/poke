package scan.ai.inference.ollama

internal data class DTOOllamaChatReq(
    val model:String,
    val messages:ArrayList<DTOOllamaMessage>,
    val stream: Boolean? = null
)

internal data class DTOOllamaMessage(
    val role:String,
    var content:String,
    val images:ArrayList<String>? = null
)