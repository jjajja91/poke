package server.entity.r2dbc

import scan.sql.Table
import java.time.LocalDateTime

class _Version(
    val _version_rowid: Int,
    val name_kr: String,
    val name_jp: String,
    val name_en: String,
    val groupkey: String,
    val regdate: LocalDateTime,
): Table