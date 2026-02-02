package scan.ai.inference.dto

data class DTOChatRes(
    val choices: List<Choice>,
    val id: String? = null,
    val usage: Usage? = null,
    val model: String? = null
) {
    data class StreamRes(
        val id: String,
        val created: Long,
        val model: String,
        val choices: List<StreamChoice>
    ) {
        data class StreamChoice(
            val index: Int,
            val delta: Map<String, String>
        )
    }
    data class Choice(
        val index: Int,
        val message: ResMessage,
        val finish_reason: String? = null
    ) {

        data class ResMessage(
            val role: String,
            val content: String? = null,
            val tool_calls: List<ToolCall>? = null
        ) {
            data class ToolCall(
                val id: String,
                val type: String,
                val function: FunctionCall
            )
            data class FunctionCall(
                val name: String,
                val arguments: String
            )
        }
    }
    data class Usage(
        val prompt_tokens: Int,
        val completion_tokens: Int,
        val total_tokens: Int
    )
}