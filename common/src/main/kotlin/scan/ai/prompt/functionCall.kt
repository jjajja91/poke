package scan.ai.prompt

import scan.ai.inference.dto.DTOTool
import scan.ai.message.function.ToolFunction
import scan.ai.message.text.DTOTextMsg

fun Prompt.functionCall(fs: List<ToolFunction>): DTOTextMsg =
    DTOTextMsg(
        role = "system",
        content = buildString {
            appendLine("You are a function selector.")
            appendLine("Your ONLY job is to examine the user's question and determine which tool(s) from the list below should be called, and with what parameters.")
            appendLine("⚠️ You are NOT answering the question.")
            appendLine("⚠️ You are NOT allowed to explain, paraphrase, or generate anything else.")
            appendLine("This is a **pre-processing step**.")
            appendLine("The result of your selection will be passed to a second phase that uses the tool results to generate a final answer.")
            appendLine("---")
            appendLine("🧠 Output rules:")
            appendLine("1. If no tool is needed, respond with exactly:")
            appendLine("false")
            appendLine()
            appendLine("2. If one or more tools are needed, respond with a JSON object:")
            appendLine("{")
            appendLine("""  "toolName": { "arg1": value1, "arg2": value2 },""")
            appendLine("  ...")
            appendLine("}")
            appendLine()
            appendLine("3. Use {} if a tool takes no parameters.")
            appendLine()
            appendLine("4. Do NOT output anything other than a JSON object or `false`.")
            appendLine("No explanations, comments, or extra formatting.")
            appendLine("---")
            appendLine("📐 You must strictly follow the tool definitions provided below.")
            appendLine()
            appendLine("📦 Available tools:")
            appendLine()

            fs.forEachIndexed { idx, tf ->
                if (idx > 0) appendLine("\n---\n")

                appendLine("- name:${tf.name}")
                appendLine("- when:${tf.description}")
                appendLine("- parameters:")

                val f: DTOTool.Function.F = tf.function.function
                val params = f.parameters?.properties

                if (params == null || params.isEmpty()) {
                    appendLine("none")
                } else {
                    appendLine("{")
                    params.entries.forEachIndexed { pIdx, (k, v) ->
                        val comma = if (pIdx < params.size - 1) "," else ""
                        appendLine("""  "$k": {"what":"${escape(v.description)}","type":"${v.type}"}$comma""")
                    }
                    appendLine("}")
                }
            }

            appendLine()
            appendLine("---")
            appendLine("Now read the user’s input and output either:")
            appendLine("- A JSON object mapping tool names to arguments (following schema exactly), or The word `false`")
        }
    )

private fun escape(s: String): String =
    s.replace("\\", "\\\\").replace("\"", "\\\"")