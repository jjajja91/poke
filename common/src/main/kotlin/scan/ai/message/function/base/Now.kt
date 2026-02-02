package scan.ai.message.function.base

import scan.ai.inference.dto.DTOTool
import scan.ai.message.function.ToolFunction
import java.time.Instant

object Now: ToolFunction {
    override val name:String = "now"
    override val description:String = """Purpose: Provide the current date and time in UTC ISO-8601 format.
Trigger: Questions involving current time or date explicitly (e.g., 'what time is it?') or implicitly (e.g., comparing against today, finding recent events, or calculating time differences)."""
    override val function: DTOTool.Function = build()
    override suspend fun invoke(params:Map<String, Any?>):String
    = Instant.now().toString()
    init{register()}
}