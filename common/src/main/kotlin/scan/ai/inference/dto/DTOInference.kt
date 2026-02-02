package scan.ai.inference.dto

import scan.ai.inference.EnumInferenceProvider
import scan.ai.message.option.DTOOptionMsg
import java.time.Duration

data class DTOInferenceHttpConfig(
    val timeout: Duration = Duration.ofSeconds(30),
    val maxInMemorySize: Int = 10 * 1024 * 1024,
    val insecureSsl: Boolean = false
)

data class DTOInferenceConfig(
    val provider: EnumInferenceProvider,
    val url: String,
    val defaultOption: DTOOptionMsg,
    val http: DTOInferenceHttpConfig = DTOInferenceHttpConfig()
)