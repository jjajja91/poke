package scan.batch.dto

import scan.enum.EnumFailDomain
import java.time.LocalDate

data class DTOFail(
    var id: Long? = null,
    var domain: EnumFailDomain,
    var refId: Int,
    var error: DTOFailDetail,
    var updateDate: LocalDate? = null
)