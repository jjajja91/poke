package server.gateway.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    override suspend fun findAll(): List<DTOType> = withContext(Dispatchers.IO) {
        typeRepository.findAll().map {
            DTOType(
                typeId = it.id,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                relation = mapper.fromJson<DTOTypeRelation>(it.contents),
                regDate = it.regDate?.toLocalDate()
            )
        }
    }
    override suspend fun deleteAll() = withContext(Dispatchers.IO) { typeRepository.deleteAllInBatch() }
    override suspend fun saveAll(list: List<DTOType>) = withContext(Dispatchers.IO) {
        typeRepository.saveAll(list.map {
            EntType(
                id = it.typeId,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                contents = mapper.toJson(it.relation),
                regDate = it.regDate?.atStartOfDay()
            )
        })
        Unit
    }
}