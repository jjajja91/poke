package scan.ai.prompt

import com.fasterxml.jackson.databind.ObjectMapper
import scan.ai.inference.dto.DTOMessage
import scan.ai.message.text.DTOTextMsg

fun Prompt.functionResult(
    result: Map<String, Any?>
): List<DTOMessage> {
    val json = ObjectMapper()
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(result)

    return listOf(
        DTOTextMsg(
            role = "system",
            content = """
                You are a helpful assistant.
                The user's question has already been analyzed, and the best tools have been invoked accordingly.

                You are now provided with:
                - The user's question.
                - The tool results from the previous phase.
                - The definitions of all tools and their parameters/results.

                ---
                These tools were then executed using arguments inferred from the user's question.  
                The result of those tool calls is:

                📦 Tool result:

                $json
                ---
                Your task:

                - Use the tool result above to answer the user's original question.
                - Answer in natural, helpful language.
                - Do not call or suggest any additional tools.
                - Do not invent or guess missing values.
                - If the tool result is not enough, say so.
                - Respond concisely, unless further elaboration improves clarity.

                You already know what each tool and field means, because the full definitions are included above.
                Now combine that with the user's question to produce your answer.

                🗣️ Language Rule:
                - Match the user's original question language exactly.
                - Respond in Korean if the user's question is in Korean.
            """.trimIndent()
        )
    )
}