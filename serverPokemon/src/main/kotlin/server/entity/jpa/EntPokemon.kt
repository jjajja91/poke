package server.entity.jpa

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "pokemon")
class EntPokemon(
    @Id
    @Column(name = "pokemon_rowid")
    var id: Int = 0,

    @Column(name = "_type1_rowid")
    var type1Id: Int = 0,

    @Column(name = "_type2_rowid")
    var type2Id: Int = 0,

    @Column(name = "base_rowid")
    var baseId: Int = 0,

    @Column(name = "name_kr", nullable = false, length = 30)
    var nameKr: String = "",

    @Column(name = "name_jp", nullable = false, length = 30)
    var nameJp: String = "",

    @Column(name = "name_en", nullable = false, length = 30)
    var nameEn: String = "",

    @Column(name = "hp")
    var hp: Int = 0,

    @Column(name = "atk")
    var atk: Int = 0,

    @Column(name = "satk")
    var satk: Int = 0,

    @Column(name = "spd")
    var spd: Int = 0,

    @Column(name = "def")
    var def: Int = 0,

    @Column(name = "sdef")
    var sdef: Int = 0,

    @Column(name = "details", columnDefinition = "json")
    var details: String = "{}",

    @Column(name = "regdate", insertable = false, updatable = false)
    var regDate: LocalDateTime? = null
) {
    override fun toString(): String {
        return """
            번호: $id
            타입1: $type1Id
            타입2: $type2Id
            포켓몬 이름: $nameKr(${nameJp})
            HP: $hp
            공격: $atk
            특공: $satk
            속도: $spd
            방어: $def
            특방: $sdef
            종종값: ${hp + atk + satk + spd + def + sdef}
        """.trimIndent()
    }
}