package scan.ai.inference.dto

import java.io.File

interface DTOContent {
    val type: String

    data class Text(
        val text: String
    ) : DTOContent {
        override val type: String = "text"
    }

    data class ImageUrl(
        val image_url: MutableMap<String, String> = hashMapOf()
    ) : DTOContent {
        override val type: String = "image_url"
    }

    data class InputAudio(
        val data: String,
        val format: String
    ) : DTOContent {
        override val type: String = "input_audio"
    }

    data class ImageFile(
        var file_id: String = "",
        @Transient var file: File? = null
    ) : DTOContent {
        override val type: String = "image_file"
    }
}