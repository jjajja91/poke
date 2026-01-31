package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOAbility(
    val abilityRowid: Int,
    val nameKr: String,
    val nameJp: String,
    val nameEn: String,
    val descriptionKr: String,
    val descriptionJp: String,
    val descriptionEn: String,
    val regDate: LocalDateTime? = null,
): DTO