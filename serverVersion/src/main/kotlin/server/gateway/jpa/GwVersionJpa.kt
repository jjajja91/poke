package server.gateway.jpa

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import server.dto.DTOVersion
import server.entity.jpa.EntVersion
import server.gateway.GwVersion
import server.repository.jpa.RepoVersionJpa

@Profile("jpa")
@Component
class GwVersionJpa internal constructor(
    private val versionRepository: RepoVersionJpa
): GwVersion {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOVersion> {
        val list = versionRepository.findAll().map {
            DTOVersion(
                versionRowid = it.id,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                groupKey = it.groupKey,
                regDate = it.regDate
            )
        }
        list.forEach {
            log.info("[DB/조회/Version/JPA] ID:${it.versionRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Version/JPA]")
        versionRepository.deleteAllInBatch()
    }
    override suspend fun saveAll(list: List<DTOVersion>) {
        list.forEachIndexed { idx, version ->
            log.info("[DB/저장/Version/JPA]-$idx ID:${version.versionRowid}, 이름:${version.nameKr}")
        }
        versionRepository.saveAll(list.map {
            EntVersion(
                id = it.versionRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                groupKey = it.groupKey,
                regDate = it.regDate
            )
        })
    }
}