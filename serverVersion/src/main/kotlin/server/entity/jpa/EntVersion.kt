package server.entity.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "_version")
class EntVersion(
    @Id
    @Column(name = "_version_rowid")
    var id: Int = 0,

    @Column(name = "name_kr", nullable = false, length = 30)
    var nameKr: String = "",

    @Column(name = "name_jp", nullable = false, length = 30)
    var nameJp: String = "",

    @Column(name = "name_en", nullable = false, length = 30)
    var nameEn: String = "",

    @Column(name = "groupkey", nullable = false, length = 100)
    var groupKey: String = "",

    @Column(name = "regdate", insertable = false, updatable = false)
    var regDate: LocalDateTime? = null,
) {
    override fun toString(): String {
        return """
            번호: $id
            버전 이름: $nameKr(${nameJp})
            버전 그룹: $groupKey
        """.trimIndent()
    }
}