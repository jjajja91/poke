package scan.batch.gateway.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import scan.batch.dto.DTOFail
import scan.batch.gateway.GwFail
import scan.batch.repository.jpa.FailRow
import scan.batch.repository.jpa.RepoFailJpa
import scan.enum.EnumFailDomain
import scan.sql.fromJson
import scan.sql.toJson
import kotlin.Int

@Profile("jpa")
@Component
class GwFailJpa(
    private val repoFail: RepoFailJpa,
    private val mapper: ObjectMapper,
): GwFail {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun deleteAllByDomainAndRefIdIn(domain:EnumFailDomain, refIds: List<Int>):Long {
        log.info("[DB/삭제/Fail/JPA] 도메인: ${domain.name}, ID: ${refIds.joinToString(",")}")
        return repoFail.deleteAllByDomainAndRefIdIn(domain.tableName, refIds.toSet())
    }
    override suspend fun deleteAllByDomain(domain:EnumFailDomain):Long {
        log.info("[DB/삭제/Fail/JPA] 도메인: ${domain.name}")
        return repoFail.deleteAllByDomain(domain.tableName)
    }
    override suspend fun findAllByDomain(domain:EnumFailDomain):List<DTOFail> {
        log.info("[DB/조회/Fail/JPA] 도메인: ${domain.name}")
        return repoFail.findAllByDomain(domain.tableName).map {
            DTOFail(
                id = it.id ?: throw Throwable("not found"),
                domain = EnumFailDomain.byTableName(domain.tableName),
                refId = it.refId,
                error = mapper.fromJson(it.error),
                updateDate = it.updateDate
            )
        }
    }
    override suspend fun saveAll(list:List<DTOFail>) {
        list.forEachIndexed { idx, fail ->
            log.info("[DB/저장/Fail/JPA]-$idx 도메인:${fail.domain}, ID: ${fail.refId}, ERROR: ${fail.error.message}")
        }
        repoFail.upsertAll(
            list.map {
                FailRow(
                    domain = it.domain.tableName,
                    refId = it.refId,
                    errorJson = mapper.toJson(it.error)
                )
            }
        )
    }
}