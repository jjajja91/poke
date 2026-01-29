package scan.batch.gateway.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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
    override suspend fun deleteAllByDomainAndRefIdIn(domain:EnumFailDomain, refIds: List<Int>):Long {
        return repoFail.deleteAllByDomainAndRefIds(domain, refIds.toSet())
    }
    override suspend fun deleteAllByDomain(domain:EnumFailDomain):Long {
        return repoFail.deleteAllByDomain(domain)
    }
    override suspend fun findAllByDomain(domain:EnumFailDomain):List<DTOFail> {
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
        repoFail.addAll(list)
    }
}