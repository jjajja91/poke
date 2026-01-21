package scan.batch

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

interface RepoFail : JpaRepository<EntFail, Long>, RepoFailCustom {
    fun findAllByDomain(domain: String): List<EntFail>
    fun deleteAllByDomainAndRefIdIn(domain: String, refIds: Collection<Int>): Long
    fun deleteAllByDomain(domain: String): Long
}

interface RepoFailCustom {
    fun upsertAll(rows: List<FailRow>): Int
}

data class FailRow(
    val domain: String,
    val id: Int,
    val errorJson: String
)

@Repository
class RepoFailImpl(
    @PersistenceContext private val em: EntityManager
) : RepoFailCustom {

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
            q.setParameter(idx++, r.id)
            q.setParameter(idx++, r.errorJson)
        }
        return q.executeUpdate()
    }
}