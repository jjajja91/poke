@file:Suppress("NOTHING_TO_INLINE")

package scan.sql.where

import scan.sql.common.QueryData
import kotlin.jvm.JvmInline

@JvmInline
value class WhereAnd(val data:QueryData){
    inline fun and():Where{
        data.where().add(And)
        return Where(data)
    }
    inline fun or():Where{
        data.where().add(Or)
        return Where(data)
    }
}