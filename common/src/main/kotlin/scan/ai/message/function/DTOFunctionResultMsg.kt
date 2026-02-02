package scan.ai.message.function

import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.text.DTOTextMsg

data class DTOFunctionResultMsg(
    override val role: String = "tool",
    val tool_call_id: String,
    val name: String,
    val content: String
) : DTOMessage {
    override suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<DTOMessage.Task>): List<DTOMessage> = DTOMessage.EMPTY
    override suspend fun update(inference: InferenceEngine, request: DTOTextMsg, response: DTOTextMsg) {}
}
fun DTOMessage.Companion.functionResult(id:String, name:String, content:String):DTOFunctionResultMsg = DTOFunctionResultMsg("tool", id, name, content)