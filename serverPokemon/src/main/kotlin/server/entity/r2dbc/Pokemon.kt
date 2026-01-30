package server.entity.r2dbc

import scan.sql.Table
import java.time.LocalDateTime

class Pokemon(
    val pokemon_rowid: Int,
    val _type1_rowid: Int,
    val _type2_rowid: Int,
    val base_rowid: Int,
    val name_kr: String,
    val name_jp: String,
    val name_en: String,
    val hp: Int,
    val atk: Int,
    val satk: Int,
    val spd: Int,
    val def: Int,
    val sdef: Int,
    var details: String,
    var regdate: LocalDateTime
): Table