package server.dto

import scan.sql.DTO
import java.time.LocalDateTime

data class DTOMove(
    var moveRowid: Int,
    var typeRowid: Int,
    var nameKr: String,
    var nameJp: String,
    var nameEn: String,
    var descriptionKr: String,
    var descriptionJp: String,
    var descriptionEn: String,
    var details: DTOMoveDetail,
    var regDate: LocalDateTime? = null
): DTO

data class DTOMoveDetail(
    val accuracy: Int,
    val power: Int,
    val pp: Int,
    val damageClass: Int
): DTO