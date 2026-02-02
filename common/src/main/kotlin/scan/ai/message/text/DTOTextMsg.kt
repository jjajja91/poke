package scan.ai.message.text

import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOMessage

data class DTOTextMsg(
    override val role: String,
    val content: String
) : DTOMessage {

    override suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<DTOMessage.Task>): List<DTOMessage> = listOf(this)

    override suspend fun update(inference: InferenceEngine, request: DTOTextMsg, response: DTOTextMsg) {}
}
fun DTOMessage.Companion.user(message: String): DTOTextMsg = DTOTextMsg(role = "user", content = message)
fun DTOMessage.Companion.system(message: String): DTOTextMsg = DTOTextMsg(role = "system", content = message)
fun DTOMessage.Companion.assistant(message: String): DTOTextMsg = DTOTextMsg(role = "assistant", content = message)