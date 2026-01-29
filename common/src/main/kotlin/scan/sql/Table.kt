package scan.sql

import kotlin.reflect.KClass

interface Table
fun <T:Table> KClass<T>.tableName():String = requireNotNull(simpleName) { "Table class must have a simpleName: $this" }.lowercase()