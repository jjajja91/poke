package scan.ai.inference.gemini

internal data class DTOGeminiFileRes(val file: DTOGeminiFile)

internal data class DTOGeminiFile(
    val name:String,
    val uri:String
)