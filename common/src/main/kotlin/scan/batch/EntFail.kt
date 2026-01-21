package scan.batch

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "fail",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_fail_domain_id",
            columnNames = ["domain", "id"]
        )
    ]
)
class EntFail(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fail_rowid")
    var id: Long? = null,

    @Column(name = "domain", nullable = false, length = 30)
    var domain: String = "",

    @Column(name = "id", nullable = false)
    var refId: Int = 0,

    @Column(name = "error", columnDefinition = "json", nullable = false)
    var error: String = "{}",  // JSON string

    @Column(name = "updatedate", insertable = false, updatable = false)
    var updateDate: LocalDateTime? = null
)