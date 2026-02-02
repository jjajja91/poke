@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package scan.ai.message.modal

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOChatReq
import scan.ai.inference.dto.DTOContent
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.text.DTOTextMsg
import scan.util.web.download
import scan.util.web.toBase64
import java.io.File

data class DTOModalMsg(
    override var role: String = "user",
    var content: MutableList<DTOContent> = arrayListOf()
) : DTOMessage {
    private fun addContent(c: DTOContent): DTOModalMsg {
        content.add(c)
        return this
    }
    fun text(text: String): DTOModalMsg =
        addContent(DTOContent.Text(text))

    fun imageUrl(url: String): DTOModalMsg =
        addContent(DTOContent.ImageUrl(hashMapOf("url" to url)))

    fun imageUrlToBase64(url: String): DTOModalMsg =
        addContent(DTOContent.ImageUrl(hashMapOf("_url" to url)))

    fun imageFileId(fileId: String): DTOModalMsg =
        addContent(DTOContent.ImageFile(file_id = fileId, file = null))

    fun imageFile(file: File): DTOModalMsg =
        addContent(DTOContent.ImageFile(file_id = "", file = file))

    fun inputAudio(data: String, format: String): DTOModalMsg =
        addContent(DTOContent.InputAudio(data = data, format = format))

    override suspend fun prepare(inference: InferenceEngine, request: DTOChatReq, user: DTOTextMsg, tasks: MutableList<DTOMessage.Task>): List<DTOMessage> {

        val uploads = content.filterIsInstance<DTOContent.ImageFile>()
            .filter { it.file != null && it.file_id.isBlank() }

        if (uploads.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                uploads.forEach { img ->
                    val f = img.file ?: return@forEach
                    val res = inference.files(InferenceEngine.FilePurpose.VISION, f)
                    res?.id?.let { newId ->
                        img.file_id = newId
                        img.file = null
                    }
                }
            }
        }

        val imageUrls = content.filterIsInstance<DTOContent.ImageUrl>()
        val toBase = imageUrls.filter { it.image_url.containsKey("_url") }

        if (toBase.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                toBase.forEach { iu ->
                    val rawUrl = iu.image_url["_url"] ?: return@forEach
                    val base64 = download(rawUrl).toBase64()
                    iu.image_url["url"] = base64
                    iu.image_url.remove("_url")
                }
            }
        }

        return listOf(this)
    }

    override suspend fun update(inference: InferenceEngine, request: DTOTextMsg, response: DTOTextMsg) {}
}
fun DTOMessage.Companion.userModal(): DTOModalMsg = DTOModalMsg(role = "user", content = arrayListOf())
