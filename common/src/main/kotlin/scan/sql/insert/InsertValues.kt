package scan.sql.insert

import scan.sql.common.Bind
import scan.sql.common.Field
import scan.sql.common.FieldType
import scan.sql.common.QueryData
import scan.sql.DTO
import scan.sql.Table
import java.time.LocalDate
import kotlin.jvm.JvmInline
import kotlin.reflect.KProperty1

@JvmInline
value class InsertValues(@PublishedApi internal val data:QueryData){
    inline fun <reified T:Table, reified V:DTO> colStr(field:KProperty1<T, String>, bind:KProperty1<V, String>):InsertValues{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colNum(field:KProperty1<T, Number>, bind:KProperty1<V, Number>):InsertValues{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colBool(field:KProperty1<T, Byte>, bind:KProperty1<V, Boolean>):InsertValues{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colDate(field:KProperty1<T, LocalDate>, bind:KProperty1<V, LocalDate>):InsertValues{
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colVO(field:KProperty1<T, DTO>, bind:KProperty1<V, DTO>):InsertValues{
        if(field.returnType != bind.returnType) throw Throwable("filed&bind mismatch: ${field.returnType} | ${bind.returnType}")
        data.addBind<V>(Bind(bind.name))
        data.values().add(Field(FieldType.Column,T::class.simpleName!!, field.name, bind.name))
        return this
    }
    inline fun <reified T:Table, F> colRaw(field:KProperty1<T, F>, raw:String):InsertValues{
        data.values().add(Field(FieldType.Raw,T::class.simpleName!!, field.name, raw))
        return this
    }
}