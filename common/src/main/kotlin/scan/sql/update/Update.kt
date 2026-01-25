@file:Suppress("NOTHING_TO_INLINE")

package scan.sql.update

import scan.sql.common.Bind
import scan.sql.common.Field
import scan.sql.common.FieldType
import scan.sql.common.From
import scan.sql.common.QueryData
import scan.sql.where.Where
import scan.sql.VO
import scan.sql.Table
import java.time.LocalDate
import kotlin.jvm.JvmInline
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.typeOf

@JvmInline
value class Update(@PublishedApi internal val data:QueryData){
    inline fun <reified T:Table, reified V:VO> colStr(field:KProperty1<T, String>, bind:KProperty1<V, String>):Update{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, reified V:VO> colNum(field:KProperty1<T, Number>, bind:KProperty1<V, Number>):Update{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, reified V:VO> colBool(field:KProperty1<T, Byte>, bind:KProperty1<V, Boolean>):Update{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, reified V:VO> colDate(field:KProperty1<T, LocalDate>, bind:KProperty1<V, LocalDate>):Update{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, reified V:VO> colVO(field:KProperty1<T, VO>, bind:KProperty1<V, VO>):Update{
        if(field.returnType != bind.returnType) throw Throwable("filed&bind mismatch: ${field.returnType} | ${bind.returnType}")
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <T:Table> from(table:KClass<T>):From {
        data._table = table.simpleName!!
        return From(data)
    }
    inline fun where():Where = Where(data)
}