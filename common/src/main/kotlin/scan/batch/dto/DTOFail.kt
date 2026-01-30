package scan.batch.dto

import scan.enum.EnumFailDomain
import java.time.LocalDateTime

data class DTOFail(
    var id: Long? = null,
    var domain: EnumFailDomain,
    var refId: Int,
    var error: DTOFailDetail,
    var updateDate: LocalDateTime? = null
)