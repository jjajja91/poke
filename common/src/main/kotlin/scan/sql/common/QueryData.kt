@file:Suppress("PropertyName", "NOTHING_TO_INLINE")

package scan.sql.common

import scan.sql.VO
import kotlin.reflect.KClass

class QueryData(val type:QueryType){
    @PublishedApi internal var offset:Any = false
    @PublishedApi internal var limit:Any = false
    @PublishedApi internal var cached:Any = false

    @PublishedApi internal var _binds:ArrayList<Bind>? = null
    @PublishedApi internal inline fun binds():ArrayList<Bind>
    = _binds ?: arrayListOf<Bind>().also{_binds = it}

    @PublishedApi internal var rsType:KClass<out VO>? = null
    @PublishedApi internal inline fun <reified V:VO> addRsVO(){
        if(rsType == null) rsType = V::class else{
            if(rsType != V::class) throw Throwable("recordSet type is not match: ${rsType?.simpleName} != ${V::class.simpleName}")
        }
    }
    @PublishedApi internal var paramType:KClass<out VO>? = null
    @PublishedApi internal inline fun <reified V:VO> addBind(bind:Bind){
        if(paramType == null) paramType = V::class else{
            if(paramType != V::class) throw Throwable("param type is not match: ${paramType?.simpleName} != ${V::class.simpleName}")
        }
        binds().find{it.name == bind.name}?.let{ throw Throwable("bind name is duplicated: ${bind.name}") }
        binds().add(bind)
    }
    @PublishedApi internal inline fun <reified V:VO> addBind(bind:String) = addBind<V>(Bind(bind))
    @PublishedApi internal var _into:String = ""
    @PublishedApi internal var _insertBulk:Boolean = false
    @PublishedApi internal var _values:ArrayList<Field>? = null
    @PublishedApi internal inline fun values():ArrayList<Field>
    = _values ?: arrayListOf<Field>().also{_values = it}

    @PublishedApi internal var _table:String = ""

    @PublishedApi internal var _fields:ArrayList<Field>? = null
    @PublishedApi internal inline fun field():ArrayList<Field>
    = _fields ?: arrayListOf<Field>().also{_fields = it}
    @PublishedApi internal var _innerJoin:ArrayList<JoinData>? = null
    @PublishedApi internal inline fun innerJoin():ArrayList<JoinData>
    = _innerJoin ?: arrayListOf<JoinData>().also{_innerJoin = it}
    @PublishedApi internal var _where:ArrayList<BindQuery>? = null
    @PublishedApi internal inline fun where():ArrayList<BindQuery>
    = _where ?: arrayListOf<BindQuery>().also{_where = it}
    @PublishedApi internal var _order:ArrayList<Pair<Field, Boolean>>? = null
    @PublishedApi internal inline fun order():ArrayList<Pair<Field, Boolean>>
    = _order ?: arrayListOf<Pair<Field, Boolean>>().also{_order = it}
}