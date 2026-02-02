@file:Suppress("NOTHING_TO_INLINE")

package scan.ai.message.task

import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.text.DTOTextMsg

class DTOTaskMsg: DTOMessage{
    override val role: String? = null
    var task:DTOMessage.Task? = null
    override suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<DTOMessage.Task>): List<DTOMessage>?{
        task?.let { tasks.add(it) }
        return null
    }
    override suspend fun update(inference:InferenceEngine, request:DTOTextMsg, response:DTOTextMsg) {}
}
inline fun DTOMessage.Companion.task(block:()->DTOMessage.Task):DTOTaskMsg
= DTOTaskMsg().apply{
    task = block()
}