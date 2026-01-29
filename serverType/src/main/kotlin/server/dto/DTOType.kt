package server.dto

import scan.sql.DTO
import java.time.LocalDate

data class DTOType(
    val typeId:Int,
    val nameKr: String,
    val nameJp: String,
    val nameEn: String,
    val relation: DTOTypeRelation,
    val regDate: LocalDate? = null
): DTO

data class DTOTypeRelation(
    var relationMap:Map<Int, DTOTypeRelationDamage>
): DTO

data class DTOTypeRelationDamage(
    var damageFrom: Double,
    var damageTo: Double
): DTO