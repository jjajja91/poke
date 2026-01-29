package scan.sql.where

import scan.sql.common.Bind
import scan.sql.common.BindQuery
import scan.sql.DTO
import scan.sql.Table
import scan.sql.tableName
import kotlin.reflect.KProperty1

class JsonEqual(val table:String, val field:String, val key:String, val bind:String):BindQuery{
    override fun invoke(bindingDecorator:(String)->String):String
            = bindingDecorator(bind).let{b->"($b is NULL or`$table`.`$field`->>'$.$key' = $b)"}
}
inline fun <reified T:Table, reified TV:DTO, reified V:DTO> Where.JsonEqualStr(field:KProperty1<T, TV>, key: KProperty1<TV, String>, bind:KProperty1<V, String>):WhereAnd{
    data.addBind<V>(Bind(bind.name){ it.toString() })
    data.where().add(JsonEqual(T::class.tableName(), field.name, key.name, bind.name))
    return WhereAnd(data)
}