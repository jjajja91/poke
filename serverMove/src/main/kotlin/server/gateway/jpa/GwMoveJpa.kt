package server.gateway.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import scan.sql.fromJson
import scan.sql.toJson
import server.dto.DTOMove
import server.entity.jpa.EntMove
import server.gateway.GwMove
import server.repository.jpa.RepoMoveJpa

@Profile("jpa")
@Component
class GwMoveJpa internal constructor(
    private val moveRepository: RepoMoveJpa,
    private val mapper: ObjectMapper,
): GwMove {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOMove> {
        val list = moveRepository.findAll().map {
            DTOMove(
                moveRowid = it.id,
                typeRowid = it.typeId,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                descriptionKr = it.descriptionKr,
                descriptionJp = it.descriptionJp,
                descriptionEn = it.descriptionEn,
                details = mapper.fromJson(it.details),
                regDate = it.regDate
            )
        }
        list.forEach {
            log.info("[DB/조회/Move/JPA] ID:${it.moveRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Move/JPA]")
        moveRepository.deleteAllInBatch()
    }
    override suspend fun saveAll(list: List<DTOMove>) {
        list.forEachIndexed { idx, move ->
            log.info("[DB/저장/Move/JPA]-$idx ID:${move.moveRowid}, 이름:${move.nameKr}")
        }
        moveRepository.saveAll(list.map {
            EntMove(
                id = it.moveRowid,
                typeId = it.typeRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                descriptionKr = it.descriptionKr,
                descriptionJp = it.descriptionJp,
                descriptionEn = it.descriptionEn,
                details = mapper.toJson(it.details),
                regDate = it.regDate
            )
        })
    }
}