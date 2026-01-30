package server.gateway.jpa

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import scan.sql.fromJson
import scan.sql.toJson
import server.dto.DTOPokemon
import server.entity.jpa.EntPokemon
import server.gateway.GwPokemon
import server.repository.jpa.RepoPokemonJpa

@Profile("jpa")
@Component
class GwPokemonJpa internal constructor(
    private val pokemonRepository: RepoPokemonJpa,
    private val mapper: ObjectMapper,
): GwPokemon {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOPokemon> {
        val list = pokemonRepository.findAll().map {
            DTOPokemon(
                pokemonRowid = it.id,
                type1Rowid = it.type1Id,
                type2Rowid = it.type2Id,
                baseRowid = it.baseId,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                hp = it.hp,
                atk = it.atk,
                satk = it.satk,
                spd = it.spd,
                def = it.def,
                sdef = it.sdef,
                details = mapper.fromJson(it.details),
                regDate = it.regDate
            )
        }
        list.forEach {
            log.info("[DB/조회/Pokemon/JPA] ID:${it.pokemonRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Pokemon/JPA]")
        pokemonRepository.deleteAllInBatch()
    }
    override suspend fun saveAll(list: List<DTOPokemon>) {
        list.forEachIndexed { idx, pokemon ->
            log.info("[DB/저장/Pokemon/JPA]-$idx ID:${pokemon.pokemonRowid}, 이름:${pokemon.nameKr}")
        }
        pokemonRepository.saveAll(list.map {
            EntPokemon(
                id = it.pokemonRowid,
                type1Id = it.type1Rowid,
                type2Id = it.type2Rowid,
                baseId = it.baseRowid,
                nameKr = it.nameKr,
                nameJp = it.nameJp,
                nameEn = it.nameEn,
                hp = it.hp,
                atk = it.atk,
                satk = it.satk,
                spd = it.spd,
                def = it.def,
                sdef = it.sdef,
                details = mapper.toJson(it.details),
                regDate = it.regDate
            )
        })
    }
}