@file:Suppress("NOTHING_TO_INLINE")

package scan.sql.insert

import scan.sql.common.Bind
import scan.sql.common.Field
import scan.sql.common.FieldType
import scan.sql.common.QueryData
import scan.sql.VO
import scan.sql.Table
import java.time.LocalDate
import kotlin.reflect.KProperty1

@JvmInline
value class Insert(@PublishedApi internal val data:QueryData){
    inline fun <reified T:Table, reified V:VO> colStr(field:KProperty1<T, String>, bind:KProperty1<V, String>):InsertValues{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return InsertValues(data)
    }
    inline fun <reified T:Table, reified V:VO> colNum(field:KProperty1<T, Number>, bind:KProperty1<V, Number>):InsertValues{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return InsertValues(data)
    }
    inline fun <reified T:Table, reified V:VO> colBool(field:KProperty1<T, Byte>, bind:KProperty1<V, Boolean>):InsertValues{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return InsertValues(data)
    }
    inline fun <reified T:Table, reified V:VO> colDate(field:KProperty1<T, LocalDate>, bind:KProperty1<V, LocalDate>):InsertValues{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return InsertValues(data)
    }
    inline fun <reified T:Table, reified V:VO> colVO(field:KProperty1<T, VO>, bind:KProperty1<V, VO>):InsertValues{
        if(field.returnType != bind.returnType) throw Throwable("filed&bind mismatch: ${field.returnType} | ${bind.returnType}")
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return InsertValues(data)
    }
    inline fun <reified T:Table> cols(vararg field:KProperty1<T, *>):InsertSelect{
        data._insertBulk = true
        data.values().addAll(field.map{Field(FieldType.Column,T::class.simpleName!!, it.name, null)})
        return InsertSelect(data)
    }
}