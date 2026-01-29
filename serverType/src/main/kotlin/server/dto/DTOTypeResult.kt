package server.dto

import scan.sql.DTO
import java.time.LocalDate

class DTOTypeResult(
        val typeRowid : Int,
        val nameKr : String,
        val nameJp : String,
        val nameEn : String,
        val contents : String,
        val regDate: LocalDate
): DTO