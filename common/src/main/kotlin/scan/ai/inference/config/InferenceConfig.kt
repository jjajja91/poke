package scan.ai.inference.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import scan.ai.inference.*
import scan.ai.inference.dto.DTOInferenceConfig
import scan.ai.inference.dto.DTOInferenceHttpConfig
import scan.ai.message.option.DTOOptionMsg

@Configuration
@EnableConfigurationProperties(DTOInferenceProps::class)
class InferenceConfig {

    @Bean
    fun dtoInferenceConfig(props: DTOInferenceProps): DTOInferenceConfig {
        return DTOInferenceConfig(
            provider = EnumInferenceProvider.valueOf(props.provider),
            url = props.url,
            http = DTOInferenceHttpConfig(
                insecureSsl = props.http.insecureSsl,
                timeout = java.time.Duration.ofMillis(props.http.timeoutMs),
                maxInMemorySize = props.http.maxInMemorySize
            ),
            defaultOption = DTOOptionMsg(
                llmModel = props.defaultOption.llmModel,
                temperature = props.defaultOption.temperature,
                top_p = props.defaultOption.top_p,
                max_tokens = props.defaultOption.max_tokens,
                headers = props.defaultOption.headers.toMutableMap()
            )
        )
    }
    @Bean
    fun inferenceEngine(config: DTOInferenceConfig): InferenceEngine =
        InferenceFactory.create(config)
}