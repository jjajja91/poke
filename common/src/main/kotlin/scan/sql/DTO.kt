package scan.sql

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue

abstract class DTO
inline fun <reified T : DTO> ObjectMapper.toJson(value: T): String = writeValueAsString(value)
inline fun <reified T : DTO> ObjectMapper.fromJson(json: String):T = readValue<T>(json)
inline fun <reified T : DTO> ObjectMapper.fromMap(map:Map<String, Any>):T = convertValue<T>(map)