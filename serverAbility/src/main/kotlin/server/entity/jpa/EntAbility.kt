package server.entity.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "_ability")
class EntAbility(
    @Id
    @Column(name = "_ability_rowid")
    var id: Int = 0,

    @Column(name = "name_kr", nullable = false, length = 30)
    var nameKr: String = "",

    @Column(name = "name_jp", nullable = false, length = 30)
    var nameJp: String = "",

    @Column(name = "name_en", nullable = false, length = 30)
    var nameEn: String = "",

    @Column(name = "description_kr", nullable = false, length = 300)
    var descriptionKr: String = "",

    @Column(name = "description_jp", nullable = false, length = 300)
    var descriptionJp: String = "",

    @Column(name = "description_en", nullable = false, length = 300)
    var descriptionEn: String = "",

    @Column(name = "regdate", insertable = false, updatable = false)
    var regDate: LocalDateTime? = null,
) {
    override fun toString(): String {
        return """
            번호: $id
            특성 이름: $nameKr(${nameJp})
            특성 설명: $descriptionKr
        """.trimIndent()
    }
}