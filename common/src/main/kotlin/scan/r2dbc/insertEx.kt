@file:Suppress("SqlSourceToSinkFlow", "USELESS_CAST", "NOTHING_TO_INLINE")

package scan.r2dbc

import scan.sql.common.Bind
import scan.sql.common.FieldType
import scan.sql.common.Query
import scan.sql.common.QueryType
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle

@PublishedApi internal inline fun DatabaseClient.insertCache(query:Query, param:Map<String, Any?>, isOptional:Boolean, block:BF):Any{
    if(query.type != QueryType.INSERT) throw Throwable("query type must be INSERT")
    if(query.cached == false){
        val builder = StringBuilder("insert into`").append(query.into).append("`(")
        query.values?.forEachIndexed {idx, it->
            if(idx > 0) builder.append(',')
            builder.append('`').append(it.table).append("`.`").append(it.field).append('`')
        } ?: throw Throwable("no value")
        builder.append(')')
        query.cached = if(query.insertBulk) builder.append("values").toString() else {
            if(query.table.isEmpty()) {
                builder.append("values(")
                query.values!!.forEachIndexed {idx, it->
                    if(idx > 0) builder.append(',')
                    if(it.type == FieldType.Raw) builder.append(it.alias!!) else builder.append(binderDecorator(it.alias!!))
                }
                builder.append(')')
            }else{
                query.fieldToSQL(builder)
                query.fromToSQL(builder, true)
            }
            sql(builder.toString())
        }
    }
    return if(query.insertBulk) query.cached else query.spec(param, isOptional, block)
}
suspend inline fun DatabaseClient.insertSelect(query:Query):Long{
    if(query.binds != null) throw Throwable("binding exist")
    if(query.insertBulk) throw Throwable("bulk insert")
    if(query.table.isEmpty()) throw Throwable("no table")
    return (insertCache(query, emptyParam, true, nullBinder) as SP).fetch().rowsUpdated().awaitSingle()
}
suspend inline fun DatabaseClient.insertSelect(query:Query, param:Map<String, Any?>, isOptional:Boolean = false):Long{
    if(query.binds == null) throw Throwable("no binding")
    if(query.insertBulk) throw Throwable("bulk insert")
    if(query.table.isEmpty()) throw Throwable("no table")
    return (insertCache(query, param, isOptional, mapBinder) as SP).fetch().rowsUpdated().awaitSingle()
}
suspend inline fun DatabaseClient.insertBulk(query:Query, params:ArrayList<Map<String, Any?>>):Long{
    if(!query.insertBulk) throw Throwable("not bulk insert")
    if(query.binds != null) throw Throwable("binding exist")
    val builder = StringBuilder(insertCache(query, emptyParam, true, nullBinder) as String)
    params.forEachIndexed {idx, map->
        if(idx > 0) builder.append(",")
        builder.append("(")
        query.values!!.forEachIndexed {idx, f->
            if(idx > 0) builder.append(',')
            map[f.field]?.let{
                val v = Bind.pass(it) ?: throw Throwable("can't bind value:$it, key:${f.field}, idx:$idx")
                if(it is String) builder.append('\'').append(v).append('\'')
                else builder.append(v)
            } ?: throw Throwable("no data field: ${f.field}, index: $idx ")
        }
        builder.append(')')
    }
    return query.spec(emptyParam, true, nullBinder, sql(builder.toString())).fetch().rowsUpdated().awaitSingle()
}
suspend inline fun DatabaseClient.insert(query:Query, pkField:String, param:Map<String, Any?>, isOptional:Boolean = false):Long{
    if(query.binds == null) throw Throwable("binding is not exist")
    return (insertCache(query, param, isOptional, mapBinder) as SP)
      .filter{it.returnGeneratedValues(pkField)}
      .map{it.get(pkField, Long::class.java) ?: -1L }
      .awaitSingle()
}