package server.gateway.r2dbc

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import server.dto.DTOVersion
import server.gateway.GwVersion
import server.repository.r2dbc.RepoVersionR2dbc

@Profile("r2dbc")
@Component
class GwVersionR2dbc internal constructor(
    private val versionRepository: RepoVersionR2dbc
): GwVersion {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOVersion> {
        val list = versionRepository.getAll().map {
            DTOVersion(
                versionRowid = it.versionRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                groupKey = it.groupKey,
                regDate = it.regDate,
            )
        }.toList()
        list.forEach {
            log.info("[DB/조회/Version/R2DBC] ID:${it.versionRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Version/R2DBC]")
        val count = versionRepository.deleteAll()
        log.info("[DB/삭제완료/Version/R2DBC] 카운트:$count")
    }
    override suspend fun saveAll(list: List<DTOVersion>) {
        list.forEachIndexed { idx, version ->
            log.info("[DB/저장/Version/R2DBC]-$idx ID:${version.versionRowid}, 이름:${version.nameKr}")
        }
        val count = versionRepository.addAll(list)
        log.info("[DB/저장완료/Version/R2DBC] 카운트:$count")
    }
}