package scan.sql.where

import scan.sql.common.Bind
import scan.sql.common.BindQuery
import scan.sql.DTO
import scan.sql.Table
import kotlin.reflect.KProperty1

class JsonIn(val table:String, val field:String, val key:String, val bind:String):BindQuery{
    override fun invoke(bindingDecorator:(String)->String):String
            = bindingDecorator(bind).let{b->"($b is NULL or JSON_CONTAINS(`$table`.`$field`->'$.$key', $b, '$'))"}
}
inline fun <reified T:Table, reified TV:DTO, reified V:DTO> Where.JsonInNum(field:KProperty1<T, TV>, key: KProperty1<TV, ArrayList<out Number>>, bind:KProperty1<V, Number>):WhereAnd{
    data.addBind<V>(Bind(bind.name){ it.toString() })
    data.where().add(JsonIn(T::class.simpleName!!, field.name, key.name, bind.name))
    return WhereAnd(data)
}