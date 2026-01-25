@file:Suppress("NOTHING_TO_INLINE")

package scan.sql

import scan.sql.common.Field
import scan.sql.common.FieldType
import scan.sql.common.From
import scan.sql.common.Query
import scan.sql.common.QueryData
import scan.sql.insert.Insert
import scan.sql.insert.InsertValues
import scan.sql.update.Update
import scan.sql.where.WhereAnd
import kotlin.reflect.KProperty1

inline fun Insert.build():Query = Query(data)
inline fun InsertValues.build():Query = Query(data)
inline fun Update.build():Query = Query(data)
inline fun WhereAnd.build():Query = Query(data)
inline fun From.build():Query = Query(data)

@PublishedApi internal inline fun <reified T:Table> order(data:QueryData, field:KProperty1<T, *>, isAsc:Boolean){data.order().add(Field(FieldType.Projection,T::class.simpleName!!, field.name, null) to isAsc)}
inline fun <reified T:Table> WhereAnd.orderASC(field:KProperty1<T, *>):WhereAnd = also { order(data, field, true) }
inline fun <reified T:Table> From.orderASC(field:KProperty1<T, *>):From = also { order(data, field, true) }
inline fun <reified T:Table> WhereAnd.orderDESC(field:KProperty1<T, *>):WhereAnd = also { order(data, field, false) }
inline fun <reified T:Table> From.orderDESC(field:KProperty1<T, *>):From = also { order(data, field, false) }
inline fun WhereAnd.limit(limit:Int):WhereAnd = also {data.limit = limit}
inline fun From.limit(limit:Int):From = also {data.limit = limit}
inline fun WhereAnd.offset(offset:Int):WhereAnd = also {data.offset = offset}
inline fun From.offset(offset:Int):From = also {data.offset = offset}
inline fun <reified V:VO> WhereAnd.limit(limit:KProperty1<V, Int>):WhereAnd = also{
    data.addBind<V>(limit.name)
    data.limit = limit.name
}
inline fun <reified V:VO> From.limit(limit:KProperty1<V, Int>):From = also{
    data.addBind<V>(limit.name)
    data.limit = limit.name
}
inline fun <reified V:VO> WhereAnd.offset(offset:KProperty1<V, Int>):WhereAnd = also{
    data.addBind<V>(offset.name)
    data.offset = offset.name
}
inline fun <reified V:VO> From.offset(offset:KProperty1<V, Int>):From = also{
    data.addBind<V>(offset.name)
    data.offset = offset.name
}