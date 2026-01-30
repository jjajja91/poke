package server.gateway.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import scan.sql.fromJson
import server.dto.DTOPokemon
import server.gateway.GwPokemon
import server.repository.r2dbc.RepoPokemonR2dbc

@Profile("r2dbc")
@Component
class GwPokemonR2dbc internal constructor(
    private val pokemonRepository: RepoPokemonR2dbc,
    private val mapper: ObjectMapper,
): GwPokemon {
    private val log = LoggerFactory.getLogger(javaClass)
    override suspend fun findAll(): List<DTOPokemon> {
        val list = pokemonRepository.getAll().map {
            DTOPokemon(
                pokemonRowid = it.pokemonRowid,
                type1Rowid = it.type1Rowid,
                type2Rowid = it.type2Rowid,
                baseRowid = it.baseRowid,
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
                regDate = it.regDate,
            )
        }.toList()
        list.forEach {
            log.info("[DB/조회/Pokemon/R2DBC] ID:${it.pokemonRowid}, 이름:${it.nameKr}")
        }
        return list
    }
    override suspend fun deleteAll() {
        log.info("[DB/삭제/Pokemon/R2DBC]")
        val count = pokemonRepository.deleteAll()
        log.info("[DB/삭제완료/Pokemon/R2DBC] 카운트$count")
    }
    override suspend fun saveAll(list: List<DTOPokemon>) {
        list.forEachIndexed { idx, pokemon ->
            log.info("[DB/저장/Pokemon/R2DBC]-$idx ID:${pokemon.pokemonRowid}, 이름:${pokemon.nameKr}")
        }
        val count = pokemonRepository.addAll(list)
        log.info("[DB/저장완료/Pokemon/R2DBC] 카운트:$count")
    }
}