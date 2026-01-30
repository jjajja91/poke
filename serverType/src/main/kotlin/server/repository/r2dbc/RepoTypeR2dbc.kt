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
import server.dto.DTOType
import server.dto.DTOTypeResult
import server.entity.r2dbc._Type

@Repository
@Profile("r2dbc")
internal class RepoTypeR2dbc(
    private val db: DatabaseClient,
    private val mapper: ObjectMapper
) {
    suspend fun add(param: DTOType): Long {
        return db.insert(
            mapper,
            qAdd,
            _Type::_type_rowid,
            TypeAddParam(
                typeRowid = param.typeRowid,
                nameKr = param.nameKr,
                nameJp = param.nameJp,
                nameEn = param.nameEn,
                contents = mapper.toJson(param.relation)
            ),
            false
        )
    }

    suspend fun addAll(param: List<DTOType>): Long {
        return db.insertBulk(
            mapper,
            qAddAll,
            ArrayList(param.map {
                TypeAddParam(
                    typeRowid = it.typeRowid,
                    nameKr = it.nameKr,
                    nameJp = it.nameJp,
                    nameEn = it.nameEn,
                    contents = mapper.toJson(it.relation)
                )
            })
        )
    }
    private class TypeAddParam(
        val typeRowid : Int,
        val nameKr : String,
        val nameJp : String,
        val nameEn : String,
        val contents : String
    ):DTO
    private val qAdd = scan.sql.insert(_Type::class)
        .colNum(_Type::_type_rowid, TypeAddParam::typeRowid)
        .colStr(_Type::name_kr, TypeAddParam::nameKr)
        .colStr(_Type::name_jp, TypeAddParam::nameJp)
        .colStr(_Type::name_en, TypeAddParam::nameEn)
        .colStr(_Type::contents, TypeAddParam::contents)
        .colRaw(_Type::regdate, "utc_timestamp()")
        .build()

    private val qAddAll = scan.sql.insertBulk(_Type::class)
        .colNum(_Type::_type_rowid, TypeAddParam::typeRowid)
        .colStr(_Type::name_kr, TypeAddParam::nameKr)
        .colStr(_Type::name_jp, TypeAddParam::nameJp)
        .colStr(_Type::name_en, TypeAddParam::nameEn)
        .colStr(_Type::contents, TypeAddParam::contents)
        .colRaw(_Type::regdate, "utc_timestamp()")
        .build()

    suspend fun getAll(): Flow<DTOTypeResult> {
        return db.select<DTOTypeResult>(mapper, qList)
    }

    private val qList = scan.sql.select()
        .colNum(_Type::_type_rowid, DTOTypeResult::typeRowid)
        .colStr(_Type::name_kr, DTOTypeResult::nameKr)
        .colStr(_Type::name_jp, DTOTypeResult::nameJp)
        .colStr(_Type::name_en, DTOTypeResult::nameEn)
        .colStr(_Type::contents, DTOTypeResult::contents)
        .colDate(_Type::regdate, DTOTypeResult::regDate)
        .from(_Type::class)
        .build()

    suspend fun deleteAll():Long {
        return db.delete(qDelete)
    }
    private val qDelete = scan.sql.delete(_Type::class).build()
}