package scan.batch.dto

import scan.sql.DTO
import java.time.LocalDate

class DTOFailResult(
        val failRowid : Long,
        val domain : String,
        val id : Int,
        val error : String,
        val updateDate: LocalDate
): DTO