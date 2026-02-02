package scan.ai.inference.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ai.inference")
data class DTOInferenceProps(
    val provider: String,
    val url: String,
    val http: HttpProps = HttpProps(),
    val defaultOption: DefaultOptionProps = DefaultOptionProps()
) {
    data class HttpProps(
        val insecureSsl: Boolean = false,
        val timeoutMs: Long = 30000,
        val maxInMemorySize: Int = 10 * 1024 * 1024
    )
    data class DefaultOptionProps(
        val llmModel: String = "",
        val temperature: Double = -100.0,
        val top_p: Double = -100.0,
        val max_tokens: Int = -100,
        val headers: Map<String, String> = emptyMap()
    )
}