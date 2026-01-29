@file:Suppress("NOTHING_TO_INLINE")

package scan.sql.select

import scan.sql.common.Field
import scan.sql.common.FieldType
import scan.sql.common.From
import scan.sql.common.QueryData
import scan.sql.DTO
import scan.sql.Table
import scan.sql.tableName
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@JvmInline
value class Select(@PublishedApi internal val data:QueryData){
    inline fun <reified T:Table> cols(vararg field:KProperty1<T, *>):Select{
        data.field().addAll(field.map{Field(FieldType.Projection,T::class.tableName(), it.name, null)})
        return this
    }
    inline fun <reified T:Table> col(field:KProperty1<T, *>, alias:String):Select{
        data.field().add(Field(FieldType.Projection,T::class.tableName(), field.name, alias))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colStr(field:KProperty1<T, String>, alias:KProperty1<V, String>):Select{
        data.addRsVO<V>()
        data.field().add(Field(FieldType.Projection,T::class.tableName(), field.name, alias.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colNum(field:KProperty1<T, Number>, alias:KProperty1<V, Number>):Select{
        data.addRsVO<V>()
        data.field().add(Field(FieldType.Projection,T::class.tableName(), field.name, alias.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colBool(field:KProperty1<T, Byte>, alias:KProperty1<V, Boolean>):Select{
        data.addRsVO<V>()
        data.field().add(Field(FieldType.Projection,T::class.tableName(), field.name, alias.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colDate(field:KProperty1<T, LocalDate>, alias:KProperty1<V, LocalDate>):Select{
        data.addRsVO<V>()
        data.field().add(Field(FieldType.Projection,T::class.tableName(), field.name, alias.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> colDTO(field:KProperty1<T, DTO>, alias:KProperty1<V, DTO>):Select{
        if(field.returnType != alias.returnType) throw Throwable("filed&alias mismatch: ${field.returnType} | ${alias.returnType}")
        data.addRsVO<V>()
        data.field().add(Field(FieldType.Projection,T::class.tableName(), field.name, alias.name))
        return this
    }
    inline fun <reified T:Table, reified V:DTO> count(field:KProperty1<T, *>, alias:KProperty1<V, Int>):SelectFrom{
        data.addRsVO<V>()
        data.field().add(Field(FieldType.Count,T::class.tableName(), field.name, alias.name))
        return SelectFrom(data)
    }
    inline fun <reified T:Table, reified V:DTO> countDistinct(field:KProperty1<T, *>, alias:KProperty1<V, Int>):SelectFrom{
        data.addRsVO<V>()
        data.field().add(Field(FieldType.CountDistinct,T::class.tableName(), field.name, alias.name))
        return SelectFrom(data)
    }
    inline fun <reified V:DTO> countAll(alias:KProperty1<V, Int>):SelectFrom{
        data.addRsVO<V>()
        data.field().add(Field(FieldType.CountAll,"", "", alias.name))
        return SelectFrom(data)
    }
    inline fun countAll(alias:String):SelectFrom{
        data.field().add(Field(FieldType.CountAll,"", "", alias))
        return SelectFrom(data)
    }
    inline fun <T:Table> from(table:KClass<T>):From {
        data._table = table.tableName()
        return From(data)
    }
}