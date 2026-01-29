package scan.sql.common

import java.time.LocalDate
import java.time.LocalDateTime


class Bind(
    val name: String,
    val converter: (Any) -> Any? = pass
) {
    class ListBind(val list: Any?)
    companion object {
        val pass: (Any) -> Any? = { value ->
            when (value) {
                is List<*> -> ListBind(value.ifEmpty { null })
                is Boolean -> if (value) 1 else 0
                is LocalDate -> value.toString()
                is LocalDateTime -> value.toString()
                else -> value
            }
        }
    }
}