@file:Suppress("NOTHING_TO_INLINE")

package scan.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import scan.sql.common.Bind
import scan.sql.common.Query
import scan.sql.DTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.r2dbc.core.DatabaseClient
import scan.sql.fromMap

@PublishedApi internal val binderDecorator:(String)->String = {":$it"}

internal typealias SP = DatabaseClient.GenericExecuteSpec //r2dbc가 문자열을 받아서 sql쿼리객체로 바꾼 것
internal typealias BF = SP.(Boolean, Bind, Map<String, Any?>)->SP // spec은 bind할때마다 새 spec반환함=>최종 마지막 spec으로 실행해야 함
@PublishedApi internal inline fun Query.spec(param:Map<String, Any?>, isOptional:Boolean, block:BF, s:SP? = null):SP
= (s ?: cached as SP).let {sp->
    binds?.fold(sp){acc, it->acc.block(isOptional, it, param)} ?: sp
}
@PublishedApi internal val emptyParam:Map<String, Any?> = emptyMap()
@PublishedApi internal val anyType:Class<Any> = Any::class.java
@PublishedApi internal val nullBinder:BF = {_, b, _->bindNull(b.name, anyType)}
@PublishedApi internal val mapBinder:BF = {isOptional, b, param->
    val k = b.name
    param[k]?.let{
        b.converter(it)?.let{v->
            when(v) {
                is Bind.ListBind -> {
                    if(v.list == null) {
                        bindNull(k, anyType).bindNull("$k#", anyType)
                    } else {
                        bind(k, v.list).bind("$k#", 1)
                    }
                }
                else -> bind(k, v)
            }
        } ?: bindNull(k, anyType)
    } ?: if(isOptional) bindNull(k, anyType) else throw Throwable("param[$k] not found")
}
@PublishedApi internal inline fun Map<String, Any>.db2VO():Map<String, Any>
= mapValues {(k, v) ->
    when(v){
        //db가 준타입을 우리타입으로
        else->v
    }
}
@PublishedApi internal inline fun <reified R:DTO> Flow<Map<String, Any>>.toDTO(mapper: ObjectMapper):Flow<R>
= map{ mapper.fromMap<R>(it) }
@PublishedApi internal inline fun Query.joinToSQL(builder:StringBuilder) {
    innerJoin?.forEach {
        builder.append("\nleft join `").append(it.fromTable).append("` on `")
            .append(it.fromTable).append("`.`").append(it.fromField).append("`=`")
            .append(it.toTable).append("`.`").append(it.toField).append('`')
    }
}
@PublishedApi internal inline fun Query.whereToSQL(builder:StringBuilder){
    where?.let{
        builder.append("\nwhere ")
        it.forEach{builder.append(it(binderDecorator))}
    }
}
@PublishedApi internal inline fun Query.fieldToSQL(builder:StringBuilder) {
    builder.append("select ")
    fields!!.forEachIndexed {idx, it->
        if(idx > 0) builder.append(',')
        builder.append('`').append(it.table).append("`.`").append(it.field).append('`')
        it.alias?.let {builder.append("as`").append(it).append('`')}
    }
}
@PublishedApi internal inline fun Query.fromToSQL(builder:StringBuilder, isOffset:Boolean){
    builder.append(" from`").append(table).append('`')
    joinToSQL(builder)
    whereToSQL(builder)
    order?.fold(builder.append("\norder by ")) {acc, (f, o)->
        acc.append(f.table).append('.').append(f.field).append(if(!o) " desc " else " ")
    }
    when(val v = limit){
        is Int -> builder.append(" limit ").append(v)
        is String -> builder.append(" limit ").append(binderDecorator(v))
    }
    if(isOffset) when(val v = offset){
        is Int -> builder.append(" offset ").append(v)
        is String -> builder.append(" offset ").append(binderDecorator(v))
    }
}
