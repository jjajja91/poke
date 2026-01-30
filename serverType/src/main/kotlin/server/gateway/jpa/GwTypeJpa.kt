package server.gateway.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import scan.sql.fromJson
import scan.sql.toJson
import server.dto.DTOType
import server.dto.DTOTypeRelation
import server.entity.jpa.EntType
import server.gateway.GwType
import server.repository.jpa.RepoTypeJpa

@Profile("jpa")
@Component
class GwTypeJpa internal constructor(
    private val typeRepository: RepoTypeJpa,
    private val mapper: ObjectMapper,
): GwType {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOType> {
        val list = typeRepository.findAll().map {
            DTOType(
                typeRowid = it.id,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                relation = mapper.fromJson<DTOTypeRelation>(it.contents),
                regDate = it.regDate
            )
        }
        list.forEach {
            log.info("[DB/조회/Type/JPA] ID:${it.typeRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Type/JPA]")
        typeRepository.deleteAllInBatch()
    }
    override suspend fun saveAll(list: List<DTOType>) {
        list.forEachIndexed { idx, type ->
            log.info("[DB/저장/Type/JPA]-$idx ID:${type.typeRowid}, 이름:${type.nameKr}")
        }
        typeRepository.saveAll(list.map {
            EntType(
                id = it.typeRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                contents = mapper.toJson(it.relation),
                regDate = it.regDate
            )
        })
    }
}