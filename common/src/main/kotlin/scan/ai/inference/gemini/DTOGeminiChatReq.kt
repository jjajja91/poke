package scan.ai.inference.gemini

import scan.ai.inference.dto.DTOTool

internal data class DTOGeminiChatReq(
    val generationConfig:DTOGeminiGenerationConfig? = null,
    val contents: ArrayList<DTOGeminiReqContents> = arrayListOf(),
    var tools:ArrayList<DTOGeminiTool>? = null,
    var system_instruction:DTOGeminiInstruction? = null
)

internal data class DTOGeminiGenerationConfig(
    val temperature:Double? = null
)

internal sealed class DTOGeminiPart {
    internal data class DTOGeminiText(val text:String):DTOGeminiPart()
    internal data class DTOGeminiFile(val file_data:DTOGeminiFileData):DTOGeminiPart()
    internal data class DTOGeminiBase64(val inline_data:DTOGeminiInlineData):DTOGeminiPart()
}

internal data class DTOGeminiInlineData(
    val data:String,
    val mime_type:String
)
internal data class DTOGeminiFileData(
    val file_uri:String,
    val mime_type:String
)
internal data class DTOGeminiReqContents(
    val role:String,
    val parts:ArrayList<DTOGeminiPart>
)
internal data class DTOGeminiInstruction(val parts: ArrayList<DTOGeminiPart>)
internal data class DTOGeminiTool(val functionDeclarations:ArrayList<DTOTool.Function.F>)