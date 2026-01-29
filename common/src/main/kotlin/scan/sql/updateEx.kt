@file:Suppress("NOTHING_TO_INLINE")

package scan.sql

import scan.sql.common.QueryData
import scan.sql.common.QueryType
import scan.sql.update.Update
import kotlin.reflect.KClass

inline fun <reified T:Table> update(table:KClass<T>):Update = Update(QueryData(QueryType.UPDATE)).apply{
    data._into = table.tableName()
}