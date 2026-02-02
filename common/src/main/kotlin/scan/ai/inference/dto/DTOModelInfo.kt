package scan.ai.inference.dto

data class DTOModelInfo(
    val inf: List<String>,
    val name: String,
    val type: String,
    val isTool: Boolean? = null,
    val isVision: Boolean? = null,
    val l2norm: Boolean? = null,
)