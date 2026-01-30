package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

class DTOVersionResult(
        val versionRowid : Int,
        val nameKr : String,
        val nameJp : String,
        val nameEn : String,
        val groupKey : String,
        val regDate: LocalDateTime
): DTO