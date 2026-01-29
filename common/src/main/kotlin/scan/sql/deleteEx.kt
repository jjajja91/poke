@file:Suppress("NOTHING_TO_INLINE")

package scan.sql

import scan.sql.common.From
import scan.sql.common.QueryData
import scan.sql.common.QueryType
import kotlin.reflect.KClass

inline fun <reified T:Table> delete(table:KClass<T>):From = From(QueryData(QueryType.DELETE)).apply{
    data._table = table.tableName()
}