@file:Suppress("FunctionName")

package scan.sql.where

import scan.sql.common.Bind
import scan.sql.common.BindQuery
import scan.sql.Table
import scan.sql.VO
import kotlin.reflect.KProperty1

class Like(val table:String, val field:String, val bind:String):BindQuery{
    override fun invoke(bindingDecorator:(String)->String):String
    = bindingDecorator(bind).let{b->"($b is NULL or`$table`.`$field`like $b)"}
}
@PublishedApi internal inline fun <reified T:Table, reified V:VO> Where._like(field:KProperty1<T, String>, bind:KProperty1<V, String>, noinline converter:(Any)->Any):WhereAnd{
    data.addBind<V>(Bind(field.name, converter))
    data.where().add(Like(T::class.simpleName!!, field.name, bind.name))
    return WhereAnd(data)
}
@PublishedApi internal val likeAll:(Any)->Any = {"%$it%"}
@PublishedApi internal val likeLeft:(Any)->Any = {"%$it"}
@PublishedApi internal val likeRight:(Any)->Any = {"$it%"}
inline fun <reified T:Table, reified V:VO> Where.like(field:KProperty1<T, String>, bind:KProperty1<V, String>):WhereAnd
= _like(field, bind, likeAll)
inline fun <reified T:Table, reified V:VO> Where.likeLeft(field:KProperty1<T, String>, bind:KProperty1<V, String>):WhereAnd
= _like(field, bind, likeLeft)
inline fun <reified T:Table, reified V:VO> Where.likeRight(field:KProperty1<T, String>, bind:KProperty1<V, String>):WhereAnd
= _like(field, bind, likeRight)