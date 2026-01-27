@file:Suppress("SqlSourceToSinkFlow", "USELESS_CAST", "NOTHING_TO_INLINE")

package scan.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import scan.sql.common.FieldType
import scan.sql.common.Query
import scan.sql.common.QueryType
import scan.sql.DTO
import kotlinx.coroutines.flow.Flow
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.flow

@PublishedApi internal inline fun DatabaseClient.selectCache(query:Query, param:Map<String, Any?>, isOptional:Boolean, block:BF):SP{
    if(query.type != QueryType.SELECT) throw Throwable("query type must be SELECT")
    if(query.cached == false) {
        val builder = StringBuilder()
        query.fields?.let { fs ->
            val field = fs.first()
            when(field.type) {
                FieldType.Projection -> query.fieldToSQL(builder)
                FieldType.Count -> {
                    builder.append("select count(")
                    builder.append('`').append(field.table).append("`.`").append(field.field).append("`)")
                    field.alias?.let {builder.append("as`").append(it).append('`')}
                }
                FieldType.CountAll -> {
                    builder.append("select count(*)")
                    field.alias?.let {builder.append("as`").append(it).append('`')}
                }
                FieldType.CountDistinct -> {
                    builder.append("select count(distinct ")
                    builder.append('`').append(field.table).append("`.`").append(field.field).append("`)")
                    field.alias?.let {builder.append("as`").append(it).append('`')}
                }
                else -> throw Throwable("query type not matched")
            }
        } ?: throw Throwable("query fields must be set")

        query.fromToSQL(builder, true)
        query.cached = sql(builder.toString())
    }
    return query.spec(param, isOptional, block)
}
inline fun DatabaseClient.selectMap(query:Query):Flow<Map<String, Any>>{
    if(query.binds != null) throw Throwable("binding exist")
    return selectCache(query, emptyParam, true, nullBinder).fetch().flow()
}
inline fun DatabaseClient.selectMap(query:Query, param:Map<String, Any?>, isOptional:Boolean = false):Flow<Map<String, Any>>{
    if(query.binds == null) throw Throwable("no binding")
    return selectCache(query, param, isOptional, mapBinder).fetch().flow()
}
inline fun <reified R:DTO> DatabaseClient.select(mapper: ObjectMapper, query:Query):Flow<R> = selectMap(query).toDTO(mapper)
inline fun <reified R:DTO> DatabaseClient.select(mapper: ObjectMapper, query:Query, param:Map<String, Any>, isOptional:Boolean = false):Flow<R>
= selectMap(query, param, isOptional).toDTO(mapper)