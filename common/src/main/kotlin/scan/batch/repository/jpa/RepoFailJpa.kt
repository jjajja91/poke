package scan.batch.repository.jpa

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import scan.batch.entity.jpa.EntFail

interface RepoFailJpa : JpaRepository<EntFail, Long>, RepoFailJpaCustom {
    fun findAllByDomain(domain: String): List<EntFail>
    fun deleteAllByDomainAndRefIdIn(domain: String, refIds: Collection<Int>): Long
    fun deleteAllByDomain(domain: String): Long
}

interface RepoFailJpaCustom {
    fun upsertAll(rows: List<FailRow>): Int
}

data class FailRow(
    val domain: String,
    val refId: Int,
    val errorJson: String
)

@Repository
@Profile("jpa")
class RepoFailJpaImpl: RepoFailJpaCustom {

    @PersistenceContext
    private lateinit var em: EntityManager

    @Transactional
    override fun upsertAll(rows: List<FailRow>): Int {
        if (rows.isEmpty()) return 0

        val valuesSql = rows.joinToString(",") { "(?,?,?)" }
        val sql = """
            INSERT INTO `fail` (`domain`, `id`, `error`)
            VALUES $valuesSql
            ON DUPLICATE KEY UPDATE
                `error` = VALUES(`error`),
                `updatedate` = CURRENT_TIMESTAMP(3)
        """.trimIndent()

        val q = em.createNativeQuery(sql)
        var idx = 1
        rows.forEach { r ->
            q.setParameter(idx++, r.domain)
            q.setParameter(idx++, r.refId)
            q.setParameter(idx++, r.errorJson)
        }
        return q.executeUpdate()
    }
}