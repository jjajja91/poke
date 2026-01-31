package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOMove(
    val moveRowid: Int,
    val typeRowid: Int,
    val nameKr: String,
    val nameJp: String,
    val nameEn: String,
    val descriptionKr: String,
    val descriptionJp: String,
    val descriptionEn: String,
    val details: DTOMoveDetail,
    val regDate: LocalDateTime? = null
): DTO

data class DTOMoveDetail(
    val accuracy: Int,
    val power: Int,
    val pp: Int,
    val damageClass: Int
): DTO