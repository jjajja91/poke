package scan.batch.entity.r2dbc

import scan.sql.Table
import java.time.LocalDateTime

class Fail(
    val fail_rowid:Long,
    val domain: String,
    val id: Int,
    val error: String,
    val updatedate: LocalDateTime
): Table