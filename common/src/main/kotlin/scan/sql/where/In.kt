package scan.sql.where

import scan.sql.common.Bind
import scan.sql.common.BindQuery
import scan.sql.DTO
import scan.sql.Table
import kotlin.reflect.KProperty1

class In(val table:String, val field:String, val bind:String):BindQuery{
    override fun invoke(bindingDecorator:(String)->String):String
    = bindingDecorator(bind).let{b->"($b# is NULL or`$table`.`$field` in ($b))"}
}
inline fun <reified T:Table, reified V:DTO> Where.InStr(field:KProperty1<T, String>, bind:KProperty1<V, ArrayList<String>>):WhereAnd{
    data.addBind<V>(Bind(bind.name))
    data.where().add(In(T::class.simpleName!!, field.name, bind.name))
    return WhereAnd(data)
}
inline fun <reified T:Table, reified V:DTO> Where.InNumber(field:KProperty1<T, Number>, bind:KProperty1<V, ArrayList<out Number>>):WhereAnd{
    data.addBind<V>(Bind(bind.name))
    data.where().add(In(T::class.simpleName!!, field.name, bind.name))
    return WhereAnd(data)
}