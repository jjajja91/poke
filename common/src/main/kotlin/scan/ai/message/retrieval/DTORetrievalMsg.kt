package scan.ai.message.retrieval

import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.text.DTOTextMsg
import scan.util.coroutine.awaitAll
import java.io.File

class DTORetrievalMsg: DTOMessage{
    override val role: String? = null
    var files:List<File>? = null
    val ids: ArrayList<String> = arrayListOf()
    override suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<DTOMessage.Task>):List<DTOMessage>{
        files?.awaitAll{f->
            inference.files(InferenceEngine.FilePurpose.RETRIEVAL, f)?.id?.let{id->
                ids.add(id)
            }
        }
        request.addRetrieval(ids)
        return DTOMessage.EMPTY
    }
    override suspend fun update(inference:InferenceEngine, request:DTOTextMsg, response:DTOTextMsg) {}
}
fun DTOMessage.Companion.retrieval(files:List<File>):DTORetrievalMsg = DTORetrievalMsg().also{
    it.files = files
}