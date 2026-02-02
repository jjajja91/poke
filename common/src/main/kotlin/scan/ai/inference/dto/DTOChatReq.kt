@file:Suppress("PropertyName")

package scan.ai.inference.dto

import scan.ai.message.option.DTOOptionMsg
import scan.ai.message.text.DTOTextMsg

class DTOChatReq {
    var model: String = ""
    var messages: ArrayList<DTOMessage> = arrayListOf()

    var temperature: Double? = null
    var max_output_tokens: Int? = null
    var max_tokens: Int? = null
    var top_p: Double? = null

    var tools: MutableList<DTOTool>? = null
    var file_ids: MutableList<String>? = null
    var tool_choice: String? = null
    var stream: Boolean? = null

    var userMsg: DTOTextMsg? = null
    var optionMsg: DTOOptionMsg? = null
    val tasks: MutableList<DTOMessage.Task> = arrayListOf()

    fun addRetrieval(files: List<String>) {
        val t = tools ?: arrayListOf<DTOTool>().also { tools = it }
        val f = file_ids ?: arrayListOf<String>().also { file_ids = it }

        if (t.none { it is DTOTool.Retrieval }) t.add(DTOTool.Retrieval)
        files.forEach { if (it !in f) f.add(it) }
    }

    fun addFunction(fs: List<DTOTool.Function.F>) {
        val t = tools ?: arrayListOf<DTOTool>().also { tools = it }
        fs.forEach { f ->
            val exists = t.filterIsInstance<DTOTool.Function>()
                .any { it.function.name == f.name }
            if (!exists) t.add(DTOTool.Function(function = f))
        }
    }

    fun addFunctionTools(fs: List<DTOTool.Function>) {
        val t = tools ?: arrayListOf<DTOTool>().also { tools = it }
        fs.forEach { tool ->
            val exists = t.filterIsInstance<DTOTool.Function>()
                .any { it.function.name == tool.function.name }
            if (!exists) t.add(tool)
        }
    }
}