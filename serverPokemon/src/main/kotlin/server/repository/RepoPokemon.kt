package server.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import server.entity.EntPokemon

interface RepoPokemon : JpaRepository<EntPokemon, Int> {

    @Query(
        value = """
            SELECT COUNT(*)
            FROM `pokemon`
            WHERE `pokemon_rowid` = `base_rowid`
        """,
        nativeQuery = true
    )
    fun countBase(): Long

    @Query(
        value = """
            SELECT *
            FROM `pokemon`
            WHERE `pokemon_rowid` = `base_rowid`
            LIMIT 1 OFFSET :offset
        """,
        nativeQuery = true
    )
    fun findBaseByOffset(@Param("offset") offset: Int): EntPokemon?
}