@file:Suppress("NOTHING_TO_INLINE")

package scan.ai.inference.dto

sealed interface DTOTool {
    val type: String
    data object Retrieval : DTOTool {
        override val type: String = "retrieval"
    }
    data class Function(
        val function: F
    ) : DTOTool {
        override val type: String = "function"
        data class F(
            val name: String,
            val description: String = "",
            var parameters: P? = null
        ) {
            data class P(
                val type: String = "object",
                val required: MutableList<String> = mutableListOf(),
                val properties: MutableMap<String, Prop> = mutableMapOf(),
                val additionalProperties: Boolean = false,
            ) {
                sealed interface Prop {
                    val type: String
                    val description: String

                    data class StringProp(
                        override val description: String = ""
                    ) : Prop {
                        override val type: String = "string"
                    }

                    data class NumberProp(
                        override val description: String = ""
                    ) : Prop {
                        override val type: String = "number"
                    }

                    data class IntegerProp(
                        override val description: String = ""
                    ) : Prop {
                        override val type: String = "integer"
                    }

                    data class BooleanProp(
                        override val description: String = ""
                    ) : Prop {
                        override val type: String = "boolean"
                    }
                }
            }
        }
        private fun ensureParams(): F.P {
            val p = function.parameters
            if (p != null) return p
            return F.P().also { function.parameters = it }
        }
        private fun param(name: String, kind: String, description: String, required: Boolean) {
            val p = ensureParams()
            p.properties[name] = when (kind) {
                "string" -> F.P.Prop.StringProp(description)
                "number" -> F.P.Prop.NumberProp(description)
                "integer" -> F.P.Prop.IntegerProp(description)
                "boolean" -> F.P.Prop.BooleanProp(description)
                else -> throw IllegalArgumentException("Unknown type: $kind")
            }
            if (required && name !in p.required) p.required.add(name)
        }
        fun paramString(name: String, description: String, required: Boolean): Function {
            param(name, "string", description, required)
            return this
        }
        fun paramNumber(name: String, description: String, required: Boolean): Function {
            param(name, "number", description, required)
            return this
        }
        fun paramInteger(name: String, description: String, required: Boolean): Function {
            param(name, "integer", description, required)
            return this
        }
        fun paramBoolean(name: String, description: String, required: Boolean): Function {
            param(name, "boolean", description, required)
            return this
        }
    }
}