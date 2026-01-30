package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOMoveResult(
    var moveRowid: Int,
    var typeRowid: Int,
    var nameKr: String,
    var nameJp: String,
    var nameEn: String,
    var descriptionKr: String,
    var descriptionJp: String,
    var descriptionEn: String,
    var details: String,
    var regDate: LocalDateTime
): DTO