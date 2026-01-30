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
import scan.sql.toJson
import server.dto.DTOMove
import server.dto.DTOMoveResult
import server.entity.r2dbc._Move

@Repository
@Profile("r2dbc")
internal class RepoMoveR2dbc(
    private val db: DatabaseClient,
    private val mapper: ObjectMapper
) {
    suspend fun add(param: DTOMove): Long {
        return db.insert(
            mapper,
            qAdd,
            _Move::_move_rowid,
            MoveAddParam(
                moveRowid = param.moveRowid,
                typeRowid = param.typeRowid,
                nameKr = param.nameKr,
                nameJp = param.nameJp,
                nameEn = param.nameEn,
                descriptionKr = param.descriptionKr,
                descriptionJp = param.descriptionJp,
                descriptionEn = param.descriptionEn,
                details = mapper.toJson(param.details)
            ),
            false
        )
    }
    suspend fun addAll(param: List<DTOMove>): Long {
        return db.insertBulk(
            mapper,
            qAddAll,
            ArrayList(param.map {
                MoveAddParam(
                    moveRowid = it.moveRowid,
                    typeRowid = it.typeRowid,
                    nameKr = it.nameKr,
                    nameJp = it.nameJp,
                    nameEn = it.nameEn,
                    descriptionKr = it.descriptionKr,
                    descriptionJp = it.descriptionJp,
                    descriptionEn = it.descriptionEn,
                    details = mapper.toJson(it.details)
                )
            })
        )
    }
    private class MoveAddParam(
        val moveRowid : Int,
        val typeRowid : Int,
        val nameKr : String,
        val nameJp : String,
        val nameEn : String,
        var descriptionKr: String,
        var descriptionJp: String,
        var descriptionEn: String,
        var details: String
    ):DTO

    private val qAdd = scan.sql.insert(_Move::class)
        .colNum(_Move::_move_rowid, MoveAddParam::moveRowid)
        .colNum(_Move::_type_rowid, MoveAddParam::typeRowid)
        .colStr(_Move::name_kr, MoveAddParam::nameKr)
        .colStr(_Move::name_jp, MoveAddParam::nameJp)
        .colStr(_Move::name_en, MoveAddParam::nameEn)
        .colStr(_Move::description_kr, MoveAddParam::descriptionKr)
        .colStr(_Move::description_jp, MoveAddParam::descriptionJp)
        .colStr(_Move::description_en, MoveAddParam::descriptionEn)
        .colStr(_Move::details, MoveAddParam::details)
        .colRaw(_Move::regdate, "utc_timestamp()")
        .build()

    private val qAddAll = scan.sql.insertBulk(_Move::class)
        .colNum(_Move::_move_rowid, MoveAddParam::moveRowid)
        .colNum(_Move::_type_rowid, MoveAddParam::typeRowid)
        .colStr(_Move::name_kr, MoveAddParam::nameKr)
        .colStr(_Move::name_jp, MoveAddParam::nameJp)
        .colStr(_Move::name_en, MoveAddParam::nameEn)
        .colStr(_Move::description_kr, MoveAddParam::descriptionKr)
        .colStr(_Move::description_jp, MoveAddParam::descriptionJp)
        .colStr(_Move::description_en, MoveAddParam::descriptionEn)
        .colStr(_Move::details, MoveAddParam::details)
        .colRaw(_Move::regdate, "utc_timestamp()")
        .build()

    suspend fun getAll(): Flow<DTOMoveResult> {
        return db.select<DTOMoveResult>(mapper, qList)
    }

    private val qList = scan.sql.select()
        .colNum(_Move::_move_rowid, DTOMoveResult::moveRowid)
        .colNum(_Move::_type_rowid, DTOMoveResult::typeRowid)
        .colStr(_Move::name_kr, DTOMoveResult::nameKr)
        .colStr(_Move::name_jp, DTOMoveResult::nameJp)
        .colStr(_Move::name_en, DTOMoveResult::nameEn)
        .colStr(_Move::description_kr, DTOMoveResult::descriptionKr)
        .colStr(_Move::description_jp, DTOMoveResult::descriptionJp)
        .colStr(_Move::description_en, DTOMoveResult::descriptionEn)
        .colStr(_Move::details, DTOMoveResult::details)
        .colDate(_Move::regdate, DTOMoveResult::regDate)
        .from(_Move::class)
        .build()

    suspend fun deleteAll():Long {
        return db.delete(qDelete)
    }
    private val qDelete = scan.sql.delete(_Move::class).build()
}