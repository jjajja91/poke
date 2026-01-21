package server.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "_move")
class EntMove(
    @Id
    @Column(name = "_move_rowid")
    var id: Int = 0,

    @Column(name = "_type_rowid")
    var typeId: Int = 0,

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

    @Column(name = "details", columnDefinition = "json")
    var details: String = "{}",

    @Column(name = "regdate", insertable = false, updatable = false)
    var regDate: LocalDateTime? = null,
) {
    override fun toString(): String {
        return """
            번호: $id
            타입: $typeId
            기술 이름: $nameKr(${nameJp})
            기술 설명: $descriptionKr
        """.trimIndent()
    }
    fun getNameLanguage(): String {
        return """
            번호: $id <br>
            영어 이름: $nameEn <br>
            영어 설명: $descriptionEn <br>
            한국 이름: $nameKr <br>
            한국 설명: $descriptionKr <br>d
            일본 이름: $nameJp <br>
            일본 설명: $descriptionJp
        """.trimIndent()
    }
}