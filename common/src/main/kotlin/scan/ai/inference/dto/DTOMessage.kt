package scan.ai.inference.dto

import scan.ai.inference.InferenceEngine
import scan.ai.message.text.DTOTextMsg

interface DTOMessage {
    val role: String?
    enum class Event { PREPARED, RESPONSE_OK, RESPONSE_FAIL }
    fun interface Task {
        suspend operator fun invoke(event: Event, inference: InferenceEngine, request: DTOChatReq)
    }
    suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<Task>): List<DTOMessage>?
    suspend fun update(inference: InferenceEngine, request: DTOTextMsg, response: DTOTextMsg)
    companion object {
        val EMPTY: List<DTOMessage> = emptyList()
    }
}