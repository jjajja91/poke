package scan.ai.inference.gemini

internal data class DTOGeminiStreamChatRes(val candidates:List<DTOGeminiStreamCandidate>)

internal data class DTOGeminiStreamContent(
    val role:String,
    val parts:List<Map<String,String>>
)

internal data class DTOGeminiStreamCandidate(
    val content:DTOGeminiStreamContent,
    val finishReason:String? = null,
    val index:Int
)