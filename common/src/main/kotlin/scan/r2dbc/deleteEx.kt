@file:Suppress("SqlSourceToSinkFlow", "USELESS_CAST", "NOTHING_TO_INLINE")

package scan.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import scan.sql.common.Query
import scan.sql.common.QueryType
import scan.sql.DTO
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import scan.sql.toMap

@PublishedApi internal inline fun DatabaseClient.deleteCache(query:Query, param:Map<String, Any?>, isOptional:Boolean, block:BF):SP{
    if(query.type != QueryType.DELETE) throw Throwable("query type must be DELETE")
    if(query.cached == false) {
        val builder = StringBuilder("delete ")
        query.fromToSQL(builder, false)
        query.cached = sql(builder.toString())
    }
    return query.spec(param, isOptional, block)
}
suspend inline fun DatabaseClient.delete(query:Query):Long{
    if(query.binds != null) throw Throwable("binding exist")
    return deleteCache(query, emptyParam, true, nullBinder).fetch().rowsUpdated().awaitSingle()
}
suspend inline fun DatabaseClient.deleteMap(query:Query, param:Map<String, Any?>, isOptional:Boolean = false):Long{
    if(query.binds == null) throw Throwable("no binding")
    return deleteCache(query, param, isOptional, mapBinder).fetch().rowsUpdated().awaitSingle()
}
suspend inline fun DatabaseClient.delete(mapper: ObjectMapper, query:Query, param:DTO, isOptional:Boolean = false):Long{
    if(param::class != query.paramType) throw Throwable("param type is not match")
    return deleteMap(query, mapper.toMap(param), isOptional)
}