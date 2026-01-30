package server.gateway.r2dbc

import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import server.dto.DTOAbility
import server.gateway.GwAbility
import server.repository.r2dbc.RepoAbilityR2dbc

@Profile("r2dbc")
@Component
class GwAbilityR2dbc internal constructor(
    private val abilityRepository: RepoAbilityR2dbc
): GwAbility {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOAbility> {
        val list = abilityRepository.getAll().map {
            DTOAbility(
                abilityRowid = it.abilityRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                descriptionKr = it.descriptionKr,
                descriptionJp = it.descriptionJp,
                descriptionEn = it.descriptionEn,
                regDate = it.regDate,
            )
        }.toList()
        list.forEach {
            log.info("[DB/조회/Ability/R2DBC] ID:${it.abilityRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Ability/R2DBC]")
        val count = abilityRepository.deleteAll()
        log.info("[DB/삭제완료/Ability/R2DBC] 카운트:$count")
    }
    override suspend fun saveAll(list: List<DTOAbility>) {
        list.forEachIndexed { idx, ability ->
            log.info("[DB/저장/Ability/R2DBC]-$idx ID:${ability.abilityRowid}, 이름:${ability.nameKr}")
        }
        val count = abilityRepository.addAll(list)
        log.info("[DB/저장완료/Ability/R2DBC] 카운트:$count")
    }
}