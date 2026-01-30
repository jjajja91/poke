package server.gateway.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import scan.sql.fromJson
import server.dto.DTOType
import server.gateway.GwType
import server.repository.r2dbc.RepoTypeR2dbc

@Profile("r2dbc")
@Component
class GwTypeR2dbc internal constructor(
    private val typeRepository: RepoTypeR2dbc,
    private val mapper: ObjectMapper,
): GwType {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOType> {
        val list = typeRepository.getAll().map {
            DTOType(
                typeRowid = it.typeRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                relation = mapper.fromJson(it.contents),
                regDate = it.regDate,
            )
        }.toList()
        list.forEach {
            log.info("[DB/조회/Type/R2DBC] ID:${it.typeRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Type/R2DBC]")
        val count = typeRepository.deleteAll()
        log.info("[DB/삭제완료/Type/R2DBC] 카운트$count")
    }
    override suspend fun saveAll(list: List<DTOType>) {
        list.forEachIndexed { idx, type ->
            log.info("[DB/저장/Type/R2DBC]-$idx ID:${type.typeRowid}, 이름:${type.nameKr}")
        }
        val count = typeRepository.addAll(list)
        log.info("[DB/저장완료/Type/R2DBC] 카운트:$count")
    }
}