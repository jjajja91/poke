package server.entity.r2dbc

import scan.sql.Table
import java.time.LocalDateTime

class _Ability(
    val _ability_rowid: Int,
    val name_kr: String,
    val name_jp: String,
    val name_en: String,
    val description_kr: String,
    val description_jp: String,
    val description_en: String,
    val regdate: LocalDateTime,
): Table