package scan.sql

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue

interface DTO
inline fun <reified T : Any> ObjectMapper.toJson(value: T): String = writeValueAsString(value)
inline fun <reified T : Any> ObjectMapper.toMap(value: T): Map<String, Any?> = convertValue(value)
inline fun <reified T : Any> ObjectMapper.fromJson(json: String):T = readValue<T>(json)
inline fun <reified T : Any> ObjectMapper.fromMap(map:Map<String, Any>):T = convertValue<T>(map)