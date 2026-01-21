package server.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "_type")
class EntType(
    @Id
    @Column(name = "_type_rowid")
    var id: Int = 0,

    @Column(name = "name_kr", nullable = false, length = 30)
    var nameKr: String = "",

    @Column(name = "name_jp", nullable = false, length = 30)
    var nameJp: String = "",

    @Column(name = "name_en", nullable = false, length = 30)
    var nameEn: String = "",

    @Column(name = "contents", columnDefinition = "json")
    var contents: String = "{}",

    @Column(name = "regdate", insertable = false, updatable = false)
    var regDate: LocalDateTime? = null,
) {
    override fun toString(): String {
        return """
            번호: $id
            타입: $nameKr(${nameJp})
        """.trimIndent()
    }
}