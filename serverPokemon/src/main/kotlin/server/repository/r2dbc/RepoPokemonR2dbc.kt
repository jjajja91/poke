package server.repository.r2dbc

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import scan.r2dbc.delete
import scan.r2dbc.insert
import scan.r2dbc.insertBulk
import scan.r2dbc.select
import scan.sql.DTO
import scan.sql.build
import scan.sql.toJson
import server.dto.DTOPokemon
import server.dto.DTOPokemonResult
import server.entity.r2dbc.Pokemon

@Repository
@Profile("r2dbc")
internal class RepoPokemonR2dbc(
    private val db: DatabaseClient,
    private val mapper: ObjectMapper
) {
    suspend fun add(param: DTOPokemon): Long {
        return db.insert(
            mapper,
            qAdd,
            Pokemon::pokemon_rowid,
            PokemonAddParam(mapper, param),
            false
        )
    }

    suspend fun addAll(param: List<DTOPokemon>): Long {
        return db.insertBulk(
            mapper,
            qAddAll,
            ArrayList(param.map {
                PokemonAddParam(mapper, it)
            })
        )
    }
    private class PokemonAddParam(
        val pokemonRowid: Int,
        val type1Rowid: Int,
        val type2Rowid: Int,
        val baseRowid: Int,
        val nameKr: String,
        val nameJp: String,
        val nameEn: String,
        val hp: Int,
        val atk: Int,
        val satk: Int,
        val spd: Int,
        val def: Int,
        val sdef: Int,
        val details: String,
    ):DTO {
        companion object {
            operator fun invoke(mapper: ObjectMapper, dto: DTOPokemon) = PokemonAddParam(
                pokemonRowid = dto.pokemonRowid,
                type1Rowid = dto.type1Rowid,
                type2Rowid = dto.type2Rowid,
                baseRowid = dto.baseRowid,
                nameKr = dto.nameKr,
                nameJp = dto.nameJp,
                nameEn = dto.nameEn,
                hp = dto.hp,
                atk = dto.atk,
                satk = dto.satk,
                spd = dto.spd,
                def = dto.def,
                sdef = dto.sdef,
                details = mapper.toJson(dto.details)
            )
        }
    }

    private val qAdd = scan.sql.insert(Pokemon::class)
        .colNum(Pokemon::pokemon_rowid, PokemonAddParam::pokemonRowid)
        .colNum(Pokemon::_type1_rowid, PokemonAddParam::type1Rowid)
        .colNum(Pokemon::_type2_rowid, PokemonAddParam::type2Rowid)
        .colNum(Pokemon::base_rowid, PokemonAddParam::baseRowid)
        .colStr(Pokemon::name_kr, PokemonAddParam::nameKr)
        .colStr(Pokemon::name_jp, PokemonAddParam::nameJp)
        .colStr(Pokemon::name_en, PokemonAddParam::nameEn)
        .colNum(Pokemon::hp, PokemonAddParam::hp)
        .colNum(Pokemon::atk, PokemonAddParam::atk)
        .colNum(Pokemon::satk, PokemonAddParam::satk)
        .colNum(Pokemon::spd, PokemonAddParam::spd)
        .colNum(Pokemon::def, PokemonAddParam::def)
        .colNum(Pokemon::sdef, PokemonAddParam::sdef)
        .colStr(Pokemon::details, PokemonAddParam::details)
        .colRaw(Pokemon::regdate, "utc_timestamp()")
        .build()

    private val qAddAll = scan.sql.insertBulk(Pokemon::class)
        .colNum(Pokemon::pokemon_rowid, PokemonAddParam::pokemonRowid)
        .colNum(Pokemon::_type1_rowid, PokemonAddParam::type1Rowid)
        .colNum(Pokemon::_type2_rowid, PokemonAddParam::type2Rowid)
        .colNum(Pokemon::base_rowid, PokemonAddParam::baseRowid)
        .colStr(Pokemon::name_kr, PokemonAddParam::nameKr)
        .colStr(Pokemon::name_jp, PokemonAddParam::nameJp)
        .colStr(Pokemon::name_en, PokemonAddParam::nameEn)
        .colNum(Pokemon::hp, PokemonAddParam::hp)
        .colNum(Pokemon::atk, PokemonAddParam::atk)
        .colNum(Pokemon::satk, PokemonAddParam::satk)
        .colNum(Pokemon::spd, PokemonAddParam::spd)
        .colNum(Pokemon::def, PokemonAddParam::def)
        .colNum(Pokemon::sdef, PokemonAddParam::sdef)
        .colStr(Pokemon::details, PokemonAddParam::details)
        .colRaw(Pokemon::regdate, "utc_timestamp()")
        .build()

    suspend fun getAll(): Flow<DTOPokemonResult> {
        return db.select<DTOPokemonResult>(mapper, qList)
    }

    private val qList = scan.sql.select()
        .colNum(Pokemon::pokemon_rowid, DTOPokemonResult::pokemonRowid)
        .colNum(Pokemon::_type1_rowid, DTOPokemonResult::type1Rowid)
        .colNum(Pokemon::_type2_rowid, DTOPokemonResult::type2Rowid)
        .colNum(Pokemon::base_rowid, DTOPokemonResult::baseRowid)
        .colStr(Pokemon::name_kr, DTOPokemonResult::nameKr)
        .colStr(Pokemon::name_jp, DTOPokemonResult::nameJp)
        .colStr(Pokemon::name_en, DTOPokemonResult::nameEn)
        .colNum(Pokemon::hp, DTOPokemonResult::hp)
        .colNum(Pokemon::atk, DTOPokemonResult::atk)
        .colNum(Pokemon::satk, DTOPokemonResult::satk)
        .colNum(Pokemon::spd, DTOPokemonResult::spd)
        .colNum(Pokemon::def, DTOPokemonResult::def)
        .colNum(Pokemon::sdef, DTOPokemonResult::sdef)
        .colStr(Pokemon::details, DTOPokemonResult::details)
        .colDate(Pokemon::regdate, DTOPokemonResult::regDate)
        .from(Pokemon::class)
        .build()

    suspend fun deleteAll():Long {
        return db.delete(qDelete)
    }
    private val qDelete = scan.sql.delete(Pokemon::class).build()
}