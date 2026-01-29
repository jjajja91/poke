@file:Suppress("SqlSourceToSinkFlow", "USELESS_CAST", "NOTHING_TO_INLINE")

package scan.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import scan.sql.DTO
import scan.sql.Table
import scan.sql.common.Field
import scan.sql.common.FieldType
import scan.sql.common.Query
import scan.sql.common.QueryType
import scan.sql.toMap
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.collections.forEachIndexed
import kotlin.reflect.KProperty1

@PublishedApi internal inline fun DatabaseClient.insertCache(query: Query, param: Map<String, Any?>, isOptional: Boolean, block: BF): Any {
    if (query.type != QueryType.INSERT) {
        throw IllegalStateException("Query type must be INSERT, got: ${query.type}")
    }
    if (query.cached == false) {
        val builder = StringBuilder("insert into `").append(query.into).append("` (")
        val values = query.values ?: throw IllegalStateException("No values defined for INSERT query")
        values.forEachIndexed { idx, f ->
            if (idx > 0) builder.append(',')
            builder.append('`').append(f.field).append('`')
        }
        builder.append(')')
        query.cached = if (query.insertBulk) {
            builder.append(" values").toString()
        } else {
            if (query.table.isEmpty()) {
                builder.append(" values(")
                values.forEachIndexed { idx, f ->
                    if (idx > 0) builder.append(',')
                    if (f.type == FieldType.Raw) builder.append(f.alias!!)
                    else builder.append(binderDecorator(f.alias!!))
                }
                builder.append(')')
                sql(builder.toString())
            } else {
                query.fieldToSQL(builder)
                query.fromToSQL(builder, true)
                sql(builder.toString())
            }
        }
    }

    return if (query.insertBulk) query.cached else query.spec(param, isOptional, block)
}

suspend inline fun DatabaseClient.insertSelect(query: Query): Long {
    if (query.insertBulk) throw IllegalStateException("Cannot use insertSelect with bulk insert query")
    if (query.table.isEmpty()) throw IllegalStateException("Table name is required for insertSelect")
    if (query.binds != null) throw IllegalStateException("Binding parameters should not exist for insertSelect without params")
    val spec = insertCache(query, emptyParam, true, nullBinder) as SP
    return spec.fetch().rowsUpdated().awaitSingle().toLong()
}

suspend inline fun DatabaseClient.insertSelect(query: Query, param: Map<String, Any?>, isOptional: Boolean = false): Long {
    if (query.insertBulk) throw IllegalStateException("Cannot use insertSelect with bulk insert query")
    if (query.table.isEmpty()) throw IllegalStateException("Table name is required for insertSelect")
    if (query.binds == null) throw IllegalStateException("Binding parameters are required for insertSelect with params")
    val spec = insertCache(query, param, isOptional, mapBinder) as SP
    return spec.fetch().rowsUpdated().awaitSingle().toLong()
}

suspend fun DatabaseClient.insertBulk(query: Query, params: List<Map<String, Any?>>): Long {
    require(query.insertBulk) { "not bulk insert" }
    if (params.isEmpty()) return 0L
    val fields = query.values ?: error("no fields")
    val prefix = insertCache(query, emptyParam, true, nullBinder) as String
    val sqlText = buildBulkInsertSql(prefix, fields, params.size)
    var spec: SP = sql(sqlText)
    params.forEachIndexed { rowIndex, row ->
        fields.forEach { f ->
            if (f.type == FieldType.Raw) return@forEach
            val key = f.alias ?: error("Column field must have alias(param key). field=${f.field}")
            val bindKey = "${key}_$rowIndex"
            val value = row[key]
            spec = if (value == null) spec.bindNull(bindKey, anyType)
            else spec.bind(bindKey, value)
        }
    }
    return spec.fetch().rowsUpdated().awaitSingle().toLong()
}
@PublishedApi
internal fun buildBulkInsertSql(prefix: String, fields: List<Field>, rows: Int): String {
    val sb = StringBuilder(prefix)
    repeat(rows) { r ->
        if (r > 0) sb.append(',')
        sb.append('(')
        fields.forEachIndexed { c, f ->
            if (c > 0) sb.append(',')
            when (f.type) {
                FieldType.Raw -> sb.append(requireNotNull(f.alias) { "Raw field must have alias(raw sql)" })
                else -> {
                    val key = requireNotNull(f.alias) { "Column field must have alias(param key)" }
                    sb.append(':').append(key).append('_').append(r)
                }
            }
        }
        sb.append(')')
    }
    return sb.toString()
}
@PublishedApi internal fun bindValue(spec: SP, bindKey: String, value: Any?): SP {
    if (value == null) return spec.bindNull(bindKey, anyType)
    return when (value) {
        is String -> spec.bind(bindKey, value)
        is Number -> spec.bind(bindKey, value)
        is Boolean -> spec.bind(bindKey, value)
        is LocalDate -> spec.bind(bindKey, value)
        is LocalDateTime -> spec.bind(bindKey, value)
        else -> {
            spec.bind(bindKey, value)
        }
    }
}
suspend inline fun DatabaseClient.insert(query: Query, pkField: String, param: Map<String, Any?>, isOptional: Boolean = false): Long {
    if (query.binds == null) throw IllegalStateException("Binding parameters are required for insert")
    if (query.insertBulk) throw IllegalStateException("Use insertBulk for bulk insert")
    val spec = insertCache(query, param, isOptional, mapBinder) as SP
    return spec
        .filter { it.returnGeneratedValues(pkField) }
        .map { row -> row.get(pkField, Long::class.java) ?: -1L }
        .awaitSingle()
}
suspend inline fun <reified V : Table> DatabaseClient.insert(mapper: ObjectMapper, query: Query, pkField: KProperty1<V, Any>, param: DTO, isOptional: Boolean = false): Long {
    if (param::class != query.paramType) throw IllegalArgumentException("Parameter type mismatch - expected: ${query.paramType}, got: ${param::class}")
    return insert(query, pkField.name, mapper.toMap(param), isOptional)
}
suspend inline fun <reified V : DTO> DatabaseClient.insertBulk(mapper: ObjectMapper, query: Query, params: List<V>): Long {
    if (params.isEmpty()) return 0L
    if (V::class != query.paramType) throw IllegalArgumentException("Parameter type mismatch - expected: ${query.paramType}, got: ${V::class}")
    return insertBulk(query, params.map { mapper.toMap(it) })
}