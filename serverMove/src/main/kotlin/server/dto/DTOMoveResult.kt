package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOMoveResult(
    val moveRowid: Int,
    val typeRowid: Int,
    val nameKr: String,
    val nameJp: String,
    val nameEn: String,
    val descriptionKr: String,
    val descriptionJp: String,
    val descriptionEn: String,
    val details: String,
    val regDate: LocalDateTime
): DTO