package server.gateway.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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
    override suspend fun findAll(): List<DTOType> {
        return typeRepository.getAll().map {
            DTOType(
                typeId = it.typeRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                relation = mapper.fromJson(it.contents),
                regDate = it.regDate,
            )
        }.toList()
    }
    override suspend fun deleteAll() {
        typeRepository.deleteAll()
    }
    override suspend fun saveAll(list: List<DTOType>) {
        typeRepository.addAll(list)
    }
}