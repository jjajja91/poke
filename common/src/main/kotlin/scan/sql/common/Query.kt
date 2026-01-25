package scan.sql.common

import kotlin.jvm.JvmInline

@JvmInline
value class Query(@PublishedApi internal val data:QueryData){
    inline val values get() = data._values
    inline val insertBulk get() = data._insertBulk
    inline val into get() = data._into
    inline val binds get() = data._binds
    inline val offset get() = data.offset
    inline val limit get() = data.limit
    inline val paramType get() = data.paramType
    inline val type get() = data.type
    inline var cached get() = data.cached
                      set(value){data.cached = value}
    inline val fields get() = data._fields
    inline val table get() = data._table
    inline val innerJoin get() = data._innerJoin
    inline val where get() = data._where
    inline val order get() = data._order
}