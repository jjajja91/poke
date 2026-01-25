@file:Suppress("NOTHING_TO_INLINE")

package scan.sql.select

import scan.sql.common.From
import scan.sql.common.QueryData
import scan.sql.Table
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass

@JvmInline
value class SelectFrom(@PublishedApi internal val data:QueryData){
    inline fun <T:Table> from(table:KClass<T>):From {
        data._table = table.simpleName!!
        return From(data)
    }
}