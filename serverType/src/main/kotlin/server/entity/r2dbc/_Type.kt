package server.entity.r2dbc

import scan.sql.Table
import java.time.LocalDate

class _Type(
    val _type_rowid:Int,
    val name_kr: String,
    val name_jp: String,
    val name_en: String,
    val contents: String,
    val regdate: LocalDate
): Table