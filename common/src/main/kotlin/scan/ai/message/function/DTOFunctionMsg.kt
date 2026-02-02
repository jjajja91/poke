@file:Suppress("UNCHECKED_CAST")

package scan.ai.message.function

import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOMessage
import scan.ai.inference.dto.DTOTool
import scan.ai.message.text.DTOTextMsg

class DTOFunctionMsg : DTOMessage {
    var functions: List<DTOTool.Function.F>? = null
    var infKey: String = ""
    var model: String = ""

    override val role: String? = null

    override suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<DTOMessage.Task>): List<DTOMessage> {
        val fs = functions ?: return DTOMessage.EMPTY
        request.addFunction(fs)
        return DTOMessage.EMPTY
    }

    override suspend fun update(inference: InferenceEngine, request: DTOTextMsg, response: DTOTextMsg) {}
}

fun DTOMessage.Companion.function(fs:List<DTOTool.Function.F>, infKey:String = "", model:String = ""):DTOFunctionMsg = DTOFunctionMsg().also{
    it.functions = fs
    it.infKey = infKey
    it.model = model
}