package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOAbility(
    var abilityRowid: Int,
    var nameKr: String,
    var nameJp: String,
    var nameEn: String,
    var descriptionKr: String,
    var descriptionJp: String,
    var descriptionEn: String,
    var regDate: LocalDateTime? = null,
): DTO