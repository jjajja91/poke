@file:Suppress("NOTHING_TO_INLINE")

package scan.sql.insert

import scan.sql.common.QueryData
import scan.sql.select.Select
import kotlin.jvm.JvmInline

@JvmInline
value class InsertSelect(@PublishedApi internal val data:QueryData){
    inline fun select():Select {
        data._insertBulk = false
        return Select(data)
    }
}