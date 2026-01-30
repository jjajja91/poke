package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOAbilityResult(
    var abilityRowid: Int,
    var nameKr: String,
    var nameJp: String,
    var nameEn: String,
    var descriptionKr: String,
    var descriptionJp: String,
    var descriptionEn: String,
    var regDate: LocalDateTime,
): DTO