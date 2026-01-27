package scan.sql.where

import scan.sql.common.Bind
import scan.sql.common.BindQuery
import scan.sql.Table
import scan.sql.DTO
import java.time.LocalDate
import kotlin.reflect.KProperty1

class Under(val table:String, val field:String, val bind:String):BindQuery{
    override fun invoke(bindingDecorator:(String)->String):String
    = bindingDecorator(bind).let{b->"($b is NULL or`$table`.`$field` < $b)"}
}
inline fun <reified T:Table, reified V: DTO> Where.underDate(field:KProperty1<T, LocalDate>, bind:KProperty1<V, LocalDate>):WhereAnd{
    data.addBind<V>(Bind(bind.name))
    data.where().add(Under(T::class.simpleName!!, field.name, bind.name))
    return WhereAnd(data)
}