@file:Suppress("NOTHING_TO_INLINE")

package scan.sql.common

import scan.sql.where.Where
import scan.sql.Table
import kotlin.jvm.JvmInline
import kotlin.reflect.KProperty1

@JvmInline
value class From(@PublishedApi internal val data:QueryData){
    inline fun <reified F:Table, reified T:Table> innerJoin(from:KProperty1<F, *>, to:KProperty1<T, *>):From{
        data.innerJoin().add(
            JoinData(F::class.simpleName!!, from.name, T::class.simpleName!!, to.name)
        )
        return this
    }
    inline fun where():Where = Where(data)
}