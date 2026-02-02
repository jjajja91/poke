package scan.ai.message.function

import scan.ai.inference.dto.DTOTool

interface ToolFunction{
    companion object{
        private val functions = hashMapOf<String, ToolFunction>()
        operator fun get(name:String):ToolFunction? = functions[name]
    }
    val name:String
    val description:String
    val function:DTOTool.Function
    fun register(){functions[name] = this}
    fun build():DTOTool.Function = DTOTool.Function(
        function = DTOTool.Function.F(
            name = name,
            description = description
        )
    )
    suspend operator fun invoke(params:Map<String, Any?>):String?
}