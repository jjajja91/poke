package scan.ai.inference.gemini

internal data class DTOGeminiChatRes(val candidates:List<DTOGeminiCandidate>)

internal data class DTOGeminiResContent(
    val role:String,
    val parts:List<Map<String, String>>
)

internal data class DTOGeminiCandidate(
    val content:DTOGeminiResContent,
    val finishReason:String,
    val index:Int
)