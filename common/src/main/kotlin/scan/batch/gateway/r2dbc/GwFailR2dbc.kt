package scan.batch.gateway.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import scan.batch.dto.DTOFail
import scan.batch.gateway.GwFail
import scan.batch.repository.r2dbc.RepoFailR2dbc
import scan.enum.EnumFailDomain
import scan.sql.fromJson
import kotlin.Int

@Profile("r2dbc")
@Component
class GwFailR2dbc(
    private val repoFail: RepoFailR2dbc,
    private val mapper: ObjectMapper,
): GwFail {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun deleteAllByDomainAndRefIdIn(domain:EnumFailDomain, refIds: List<Int>):Long {
        log.info("[DB/삭제/Fail/R2DBC] 도메인: ${domain.name}, ID: ${refIds.joinToString(",")}")
        return repoFail.deleteAllByDomainAndRefIds(domain, refIds.toSet())
    }
    override suspend fun deleteAllByDomain(domain:EnumFailDomain):Long {
        log.info("[DB/삭제/Fail/R2DBC] 도메인: ${domain.name}")
        return repoFail.deleteAllByDomain(domain)
    }
    override suspend fun findAllByDomain(domain:EnumFailDomain):List<DTOFail> {
        log.info("[DB/조회/Fail/R2DBC] 도메인: ${domain.name}")
        return repoFail.getAllByDomain(domain).map {
            DTOFail(
                id = it.failRowid,
                domain = EnumFailDomain.byTableName(domain.tableName),
                refId = it.id,
                error = mapper.fromJson(it.error),
                updateDate = it.updateDate
            )
        }.toList()
    }
    override suspend fun saveAll(list:List<DTOFail>) {
        list.forEachIndexed { idx, fail ->
            log.info("[DB/저장/Fail/R2DBC]-$idx 도메인:${fail.domain}, ID: ${fail.refId}, ERROR: ${fail.error.message}")
        }
        repoFail.addAll(list)
    }
}