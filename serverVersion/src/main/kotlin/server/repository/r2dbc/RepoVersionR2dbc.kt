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
import server.dto.DTOVersion
import server.dto.DTOVersionResult
import server.entity.r2dbc._Version

@Repository
@Profile("r2dbc")
internal class RepoVersionR2dbc(
    private val db: DatabaseClient,
    private val mapper: ObjectMapper
) {
    suspend fun add(param: DTOVersion): Long {
        return db.insert(
            mapper,
            qAdd,
            _Version::_version_rowid,
            VersionAddParam(param),
            false
        )
    }

    suspend fun addAll(param: List<DTOVersion>): Long {
        return db.insertBulk(
            mapper,
            qAddAll,
            ArrayList(param.map { VersionAddParam(it) })
        )
    }
    private class VersionAddParam(
        val versionRowid:Int,
        val nameKr: String,
        val nameJp: String,
        val nameEn: String,
        val groupKey: String
    ):DTO {
        companion object {
            operator fun invoke(dto: DTOVersion) = VersionAddParam(
                versionRowid = dto.versionRowid,
                nameKr = dto.nameKr,
                nameJp = dto.nameJp,
                nameEn = dto.nameEn,
                groupKey = dto.groupKey
            )
        }
    }
    private val qAdd = scan.sql.insert(_Version::class)
        .colNum(_Version::_version_rowid, VersionAddParam::versionRowid)
        .colStr(_Version::name_kr, VersionAddParam::nameKr)
        .colStr(_Version::name_jp, VersionAddParam::nameJp)
        .colStr(_Version::name_en, VersionAddParam::nameEn)
        .colStr(_Version::groupkey, VersionAddParam::groupKey)
        .colRaw(_Version::regdate, "utc_timestamp()")
        .build()

    private val qAddAll = scan.sql.insertBulk(_Version::class)
        .colNum(_Version::_version_rowid, VersionAddParam::versionRowid)
        .colStr(_Version::name_kr, VersionAddParam::nameKr)
        .colStr(_Version::name_jp, VersionAddParam::nameJp)
        .colStr(_Version::name_en, VersionAddParam::nameEn)
        .colStr(_Version::groupkey, VersionAddParam::groupKey)
        .colRaw(_Version::regdate, "utc_timestamp()")
        .build()

    suspend fun getAll(): Flow<DTOVersionResult> {
        return db.select<DTOVersionResult>(mapper, qList)
    }

    private val qList = scan.sql.select()
        .colNum(_Version::_version_rowid, DTOVersionResult::versionRowid)
        .colStr(_Version::name_kr, DTOVersionResult::nameKr)
        .colStr(_Version::name_jp, DTOVersionResult::nameJp)
        .colStr(_Version::name_en, DTOVersionResult::nameEn)
        .colStr(_Version::groupkey, DTOVersionResult::groupKey)
        .colDate(_Version::regdate, DTOVersionResult::regDate)
        .from(_Version::class)
        .build()

    suspend fun deleteAll():Long {
        return db.delete(qDelete)
    }
    private val qDelete = scan.sql.delete(_Version::class).build()
}