package scan.batch.gateway.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    override suspend fun deleteAllByDomainAndRefIdIn(domain:EnumFailDomain, refIds: List<Int>) = withContext(Dispatchers.IO) {
        repoFail.deleteAllByDomainAndRefIdIn(domain.tableName, refIds.toSet())
    }
    override suspend fun deleteAllByDomain(domain:EnumFailDomain) = withContext(Dispatchers.IO) {
        repoFail.deleteAllByDomain(domain.tableName)
    }
    override suspend fun findAllByDomain(domain:EnumFailDomain):List<DTOFail> = withContext(Dispatchers.IO) {
        repoFail.findAllByDomain(domain.tableName).map {
            DTOFail(
                id = it.id ?: throw Throwable("not found"),
                domain = EnumFailDomain.byTableName(domain.tableName),
                refId = it.refId,
                error = mapper.fromJson(it.error),
                updateDate = it.updateDate?.toLocalDate()
            )
        }
    }
    override suspend fun saveAll(list:List<DTOFail>)= withContext(Dispatchers.IO) {
        repoFail.upsertAll(
            list.map {
                FailRow(
                    domain = it.domain.tableName,
                    refId = it.refId,
                    errorJson = mapper.toJson(it.error)
                )
            }
        )
        Unit
    }
}