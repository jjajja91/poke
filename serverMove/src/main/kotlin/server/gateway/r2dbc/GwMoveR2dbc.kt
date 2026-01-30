package server.gateway.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import scan.sql.fromJson
import server.dto.DTOMove
import server.gateway.GwMove
import server.repository.r2dbc.RepoMoveR2dbc

@Profile("r2dbc")
@Component
class GwMoveR2dbc internal constructor(
    private val moveRepository: RepoMoveR2dbc,
    private val mapper: ObjectMapper,
): GwMove {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOMove> {
        val list = moveRepository.getAll().map {
            DTOMove(
                moveRowid = it.moveRowid,
                typeRowid = it.typeRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                descriptionKr = it.descriptionKr,
                descriptionJp = it.descriptionJp,
                descriptionEn = it.descriptionEn,
                details = mapper.fromJson(it.details),
                regDate = it.regDate
            )
        }.toList()
        list.forEach {
            log.info("[DB/조회/Move/R2DBC] ID:${it.moveRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Move/R2DBC]")
        val count = moveRepository.deleteAll()
        log.info("[DB/삭제완료/Move/R2DBC] 카운트$count")
    }
    override suspend fun saveAll(list: List<DTOMove>) {
        list.forEachIndexed { idx, move ->
            log.info("[DB/저장/Move/R2DBC]-$idx ID:${move.moveRowid}, 이름:${move.nameKr}")
        }
        val count = moveRepository.addAll(list)
        log.info("[DB/저장완료/Move/R2DBC] 카운트:$count")
    }
}