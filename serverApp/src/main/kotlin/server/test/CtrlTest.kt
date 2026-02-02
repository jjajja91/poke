package server.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import scan.ai.inference.InferenceEngine
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.text.DTOTextMsg

@RestController
@RequestMapping("/test")
class CtrlTest(
    private val inference: InferenceEngine
) {
    data class Req(val q: String, val stream: Boolean? = null, val flushWords: Int? = null)

    @PostMapping("/chat")
    suspend fun chat(@RequestBody body: Req): Map<String, String> {
        val messages = arrayListOf<DTOMessage>(
            DTOTextMsg(
                role = "system", content = """
                    You are a professional Pokémon localization translator.
                    
                    Task:
                    Translate the given move/ability description into Korean and Japanese.
                    
                    STRICT RULES:
                    - Output exactly 2 sections, in this exact format and order:
                    Korean: <Korean translation>
                    Japanese: <Japanese translation>
                    
                    - Korean section MUST contain only Korean letters, spaces, numbers, and common punctuation.
                    - Japanese section MUST contain only Japanese (Kana/Kanji), spaces, numbers, and common punctuation.
                    - Absolutely NO Chinese characters, NO English words, NO explanations, NO meta comments.
                    - If you violate any rule, you must silently rewrite the entire output before responding.
                    
                    Style:
                    - Korean: concise game style, "~한다/~된다/~상태로 만든다".
                    - Japanese: concise game style, "~する。/~になることがある。".
                    
                    Do not add any other lines.
            """.trimIndent()
            ),
            DTOTextMsg(role = "user", content = body.q)
        )
        val res = inference.chatCompletion(messages)
        return mapOf("answer" to (res?.content ?: "null"))
    }

    @PostMapping(
        value = ["/chat/stream"],
        produces = [MediaType.TEXT_EVENT_STREAM_VALUE]
    )
    suspend fun stream(@RequestBody body: Req): Flow<String> = flow {
        val messages = arrayListOf<DTOMessage>(
            DTOTextMsg(
                role = "system", content = """
                    You are a professional Pokémon localization translator.
                    
                    Task:
                    Translate the given move/ability description into Korean and Japanese.
                    
                    STRICT RULES:
                    - Output exactly 2 sections, in this exact format and order:
                    Korean: <Korean translation>
                    Japanese: <Japanese translation>
                    
                    - Korean section MUST contain only Korean letters, spaces, numbers, and common punctuation.
                    - Japanese section MUST contain only Japanese (Kana/Kanji), spaces, numbers, and common punctuation.
                    - Absolutely NO Chinese characters, NO English words, NO explanations, NO meta comments.
                    - If you violate any rule, you must silently rewrite the entire output before responding.
                    
                    Style:
                    - Korean: concise game style, "~한다/~된다/~상태로 만든다".
                    - Japanese: concise game style, "~する。/~になることがある。".
                    
                    Do not add any other lines.
            """.trimIndent()
            ),
            DTOTextMsg("user", body.q)
        )
        val f = inference.chatCompletionFlow(messages, body.flushWords ?: 0)
            ?: return@flow

        f.collect { emit(it) }
    }
}
