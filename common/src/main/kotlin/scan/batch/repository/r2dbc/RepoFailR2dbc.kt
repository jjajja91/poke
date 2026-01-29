package scan.batch.repository.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import scan.batch.dto.DTOFail
import scan.batch.dto.DTOFailResult
import scan.batch.entity.r2dbc.Fail
import scan.enum.EnumFailDomain
import scan.r2dbc.delete
import scan.r2dbc.insert
import scan.r2dbc.insertBulk
import scan.r2dbc.select
import scan.sql.DTO
import scan.sql.build
import scan.sql.toJson
import scan.sql.where.InNumber
import scan.sql.where.equalStr

@Repository
@Profile("r2dbc")
class RepoFailR2dbc(
    private val db: DatabaseClient,
    private val mapper: ObjectMapper
) {
    suspend fun add(param: DTOFail): Long {
        return db.insert(
            mapper,
            qAdd,
            Fail::fail_rowid,
            FailAddParam(
                domain = param.domain.tableName,
                id = param.refId,
                error = mapper.toJson(param.error)
            ),
            false
        )
    }
    suspend fun addAll(param: List<DTOFail>): Long {
        return db.insertBulk(
            mapper,
            qAddAll,
            ArrayList(param.map {
                FailAddParam(
                    domain = it.domain.tableName,
                    id = it.refId,
                    error = mapper.toJson(it.error)
                )
            })
        )
    }

    private class FailAddParam(
        val domain : String,
        val id : Int,
        val error : String
    ):DTO

    private val qAdd = scan.sql.insert(Fail::class)
        .colStr(Fail::domain, FailAddParam::domain)
        .colNum(Fail::id, FailAddParam::id)
        .colStr(Fail::error, FailAddParam::error)
        .build()

    private val qAddAll = scan.sql.insertBulk(Fail::class)
        .colStr(Fail::domain, FailAddParam::domain)
        .colNum(Fail::id, FailAddParam::id)
        .colStr(Fail::error, FailAddParam::error)
        .build()

    suspend fun getAllByDomain(domain: EnumFailDomain): Flow<DTOFailResult> {
        return db.select<DTOFailResult>(mapper, qList, FailGetParam(
            domain = domain.tableName
        ),false)
    }

    private class FailGetParam(
        val domain : String,
        val refIds: ArrayList<Int> = arrayListOf(),
    ):DTO

    private val qList = scan.sql.select()
        .colNum(Fail::fail_rowid, DTOFailResult::failRowid)
        .colStr(Fail::domain, DTOFailResult::domain)
        .colNum(Fail::id, DTOFailResult::id)
        .colStr(Fail::error, DTOFailResult::error)
        .colDate(Fail::updatedate, DTOFailResult::updateDate)
        .from(Fail::class)
        .where().equalStr(Fail::domain, FailGetParam::domain)
        .build()

    suspend fun deleteAllByDomain(domain: EnumFailDomain):Long {
        return db.delete(mapper,qDeleteByDomain, FailGetParam(domain.tableName), false)
    }
    private val qDeleteByDomain = scan.sql.delete(Fail::class)
        .where().equalStr(Fail::domain, FailGetParam::domain)
        .build()

    suspend fun deleteAllByDomainAndRefIds(domain: EnumFailDomain, refIds:Set<Int>):Long {
        return db.delete(mapper,qDeleteByDomainAndRefIds, FailGetParam(
            domain = domain.tableName,
            refIds = ArrayList(refIds)
        ), false)
    }
    private val qDeleteByDomainAndRefIds = scan.sql.delete(Fail::class)
        .where().equalStr(Fail::domain, FailGetParam::domain)
        .and().InNumber(Fail::id, FailGetParam::refIds)
        .build()


}