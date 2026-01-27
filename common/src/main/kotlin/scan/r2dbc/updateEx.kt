@file:Suppress("SqlSourceToSinkFlow", "USELESS_CAST", "NOTHING_TO_INLINE")

package scan.r2dbc

import scan.sql.common.Query
import scan.sql.common.QueryType
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient

@PublishedApi internal inline fun DatabaseClient.updateCache(query:Query, param:Map<String, Any?>, isOptional:Boolean, block:BF):SP{
    if(query.type != QueryType.UPDATE) throw Throwable("query type must be UPDATE")
    if(query.cached == false){
        val builder = StringBuilder("update `").append(query.into).append("`set ")
        query.values?.foldIndexed(builder) {idx, acc, it->
            if(idx > 0) acc.append(',')
            acc.append('`').append(it.table).append("`.`").append(it.field).append("`=").append(binderDecorator(it.alias!!))
        } ?: throw Throwable("no value")
        if(query.table.isEmpty()) query.whereToSQL(builder)
        else query.fromToSQL(builder, false)
        query.cached = sql(builder.toString())
    }
    return query.spec(param, isOptional, block)
}
suspend inline fun DatabaseClient.updateMap(query:Query, param:Map<String, Any?>, isOptional:Boolean = false):Long{
    if(query.binds == null) throw Throwable("no binding")
    return updateCache(query, param, isOptional, mapBinder).fetch().rowsUpdated().awaitSingle()
}