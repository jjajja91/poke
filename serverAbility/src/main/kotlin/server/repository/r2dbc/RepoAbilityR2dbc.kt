package server.repository.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import scan.r2dbc.delete
import scan.r2dbc.insert
import scan.r2dbc.insertBulk
import scan.r2dbc.select
import scan.sql.DTO
import scan.sql.build
import server.dto.DTOAbility
import server.dto.DTOAbilityResult
import server.entity.r2dbc._Ability

@Repository
@Profile("r2dbc")
internal class RepoAbilityR2dbc(
    private val db: DatabaseClient,
    private val mapper: ObjectMapper
) {
    suspend fun add(param: DTOAbility): Long {
        return db.insert(
            mapper,
            qAdd,
            _Ability::_ability_rowid,
            AbilityAddParam(param),
            false
        )
    }

    suspend fun addAll(param: List<DTOAbility>): Long {
        return db.insertBulk(
            mapper,
            qAddAll,
            ArrayList(param.map { AbilityAddParam(it) })
        )
    }
    private class AbilityAddParam(
        val abilityRowid:Int,
        val nameKr: String,
        val nameJp: String,
        val nameEn: String,
        val descriptionKr: String,
        val descriptionJp: String,
        val descriptionEn: String,
    ):DTO {
        companion object {
            operator fun invoke(dto: DTOAbility) = AbilityAddParam(
                abilityRowid = dto.abilityRowid,
                nameKr = dto.nameKr,
                nameJp = dto.nameJp,
                nameEn = dto.nameEn,
                descriptionKr = dto.descriptionKr,
                descriptionJp = dto.descriptionJp,
                descriptionEn = dto.descriptionEn,
            )
        }
    }

    private val qAdd = scan.sql.insert(_Ability::class)
        .colNum(_Ability::_ability_rowid, AbilityAddParam::abilityRowid)
        .colStr(_Ability::name_kr, AbilityAddParam::nameKr)
        .colStr(_Ability::name_jp, AbilityAddParam::nameJp)
        .colStr(_Ability::name_en, AbilityAddParam::nameEn)
        .colStr(_Ability::description_kr, AbilityAddParam::descriptionKr)
        .colStr(_Ability::description_jp, AbilityAddParam::descriptionJp)
        .colStr(_Ability::description_en, AbilityAddParam::descriptionEn)
        .colRaw(_Ability::regdate, "utc_timestamp()")
        .build()

    private val qAddAll = scan.sql.insertBulk(_Ability::class)
        .colNum(_Ability::_ability_rowid, AbilityAddParam::abilityRowid)
        .colStr(_Ability::name_kr, AbilityAddParam::nameKr)
        .colStr(_Ability::name_jp, AbilityAddParam::nameJp)
        .colStr(_Ability::name_en, AbilityAddParam::nameEn)
        .colStr(_Ability::description_kr, AbilityAddParam::descriptionKr)
        .colStr(_Ability::description_jp, AbilityAddParam::descriptionJp)
        .colStr(_Ability::description_en, AbilityAddParam::descriptionEn)
        .colRaw(_Ability::regdate, "utc_timestamp()")
        .build()

    suspend fun getAll(): Flow<DTOAbilityResult> {
        return db.select<DTOAbilityResult>(mapper, qList)
    }

    private val qList = scan.sql.select()
        .colNum(_Ability::_ability_rowid, DTOAbilityResult::abilityRowid)
        .colStr(_Ability::name_kr, DTOAbilityResult::nameKr)
        .colStr(_Ability::name_jp, DTOAbilityResult::nameJp)
        .colStr(_Ability::name_en, DTOAbilityResult::nameEn)
        .colStr(_Ability::description_kr, DTOAbilityResult::descriptionKr)
        .colStr(_Ability::description_jp, DTOAbilityResult::descriptionJp)
        .colStr(_Ability::description_en, DTOAbilityResult::descriptionEn)
        .colDate(_Ability::regdate, DTOAbilityResult::regDate)
        .from(_Ability::class)
        .build()

    suspend fun deleteAll():Long {
        return db.delete(qDelete)
    }
    private val qDelete = scan.sql.delete(_Ability::class).build()
}