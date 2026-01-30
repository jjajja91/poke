package server.gateway.jpa

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import server.dto.DTOAbility
import server.entity.jpa.EntAbility
import server.gateway.GwAbility
import server.repository.jpa.RepoAbilityJpa

@Profile("jpa")
@Component
class GwAbilityJpa internal constructor(
    private val abilityRepository: RepoAbilityJpa
): GwAbility {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOAbility> {
        val list = abilityRepository.findAll().map {
            DTOAbility(
                abilityRowid = it.id,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                descriptionKr = it.descriptionKr,
                descriptionJp = it.descriptionJp,
                descriptionEn = it.descriptionEn,
                regDate = it.regDate,
            )
        }
        list.forEach {
            log.info("[DB/조회/Ability/JPA] ID:${it.abilityRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Ability/JPA]")
        abilityRepository.deleteAllInBatch()
    }
    override suspend fun saveAll(list: List<DTOAbility>) {
        list.forEachIndexed { idx, ability ->
            log.info("[DB/저장/Ability/JPA]-$idx ID:${ability.abilityRowid}, 이름:${ability.nameKr}")
        }
        abilityRepository.saveAll(list.map {
            EntAbility(
                id = it.abilityRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                descriptionKr = it.descriptionKr,
                descriptionJp = it.descriptionJp,
                descriptionEn = it.descriptionEn,
                regDate = it.regDate
            )
        })
    }
}