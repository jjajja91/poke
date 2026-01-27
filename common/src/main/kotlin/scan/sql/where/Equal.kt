package scan.sql.where

import scan.sql.common.Bind
import scan.sql.common.BindQuery
import scan.sql.DTO
import scan.sql.Table
import kotlin.reflect.KProperty1

class Equal(val table:String, val field:String, val bind:String):BindQuery{
    override fun invoke(bindingDecorator:(String)->String):String
    = bindingDecorator(bind).let{b->"($b is NULL or`$table`.`$field`=$b)"}
}
inline fun <reified T:Table, reified V:DTO> Where.equalStr(field:KProperty1<T, String>, bind:KProperty1<V, String>):WhereAnd{
    data.addBind<V>(Bind(bind.name))
    data.where().add(Equal(T::class.simpleName!!, field.name, bind.name))
    return WhereAnd(data)
}
inline fun <reified T:Table, reified V:DTO> Where.equalNum(field:KProperty1<T, Number>, bind:KProperty1<V, Number>):WhereAnd{
    data.addBind<V>(Bind(bind.name))
    data.where().add(Equal(T::class.simpleName!!, field.name, bind.name))
    return WhereAnd(data)
}