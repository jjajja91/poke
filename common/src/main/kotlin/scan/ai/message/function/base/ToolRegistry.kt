package scan.ai.message.function.base

import scan.ai.message.function.ToolFunction

object ToolRegistry {
    private val map = hashMapOf<String, ToolFunction>()
    fun register(tool: ToolFunction) {
        map[tool.name] = tool
    }
    fun get(name: String): ToolFunction? = map[name]
}