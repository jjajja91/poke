package scan.batch.entity.r2dbc

import scan.sql.Table
import java.time.LocalDate

class Fail(
    val fail_rowid:Long,
    val domain: String,
    val id: Int,
    val error: String,
    val updatedate: LocalDate
): Table