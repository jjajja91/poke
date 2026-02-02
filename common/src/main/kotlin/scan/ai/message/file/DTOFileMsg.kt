package scan.ai.message.file

import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.text.DTOTextMsg
import scan.util.coroutine.awaitAll
import java.io.File
import kotlin.collections.arrayListOf

class DTOFileMsg: DTOMessage{
    override val role: String? = null
    var files:List<Pair<InferenceEngine.FilePurpose, File>>? = null
    override suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<DTOMessage.Task>): List<DTOMessage> {
        files?.let { filePairs ->
            val fileIds = request.file_ids ?: arrayListOf<String>().also { request.file_ids = it }
            filePairs.awaitAll { (purpose, file) ->
                inference.files(purpose, file)?.let { res ->
                    fileIds.add(res.id)
                }
            }
        }
        return DTOMessage.EMPTY
    }
    override suspend fun update(inference:InferenceEngine, request:DTOTextMsg, response:DTOTextMsg) {}
}
fun DTOMessage.Companion.file(files:List<Pair<InferenceEngine.FilePurpose,File>>):DTOFileMsg = DTOFileMsg().also{
    it.files = files
}
