@file:Suppress("NOTHING_TO_INLINE")

package scan.sql

import scan.sql.common.QueryData
import scan.sql.common.QueryType
import scan.sql.insert.Insert
import kotlin.reflect.KClass

inline fun <reified T:Table> insert(table:KClass<T>):Insert = Insert(QueryData(QueryType.INSERT)).apply{
    data._into = table.simpleName!!
}