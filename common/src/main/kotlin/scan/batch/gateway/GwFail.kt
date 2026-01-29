package scan.batch.gateway

import scan.batch.dto.DTOFail
import scan.enum.EnumFailDomain

interface GwFail {
    suspend fun deleteAllByDomainAndRefIdIn(domain:EnumFailDomain, refIds: List<Int>):Long
    suspend fun deleteAllByDomain(domain:EnumFailDomain):Long
    suspend fun findAllByDomain(domain:EnumFailDomain):List<DTOFail>
    suspend fun saveAll(list:List<DTOFail>)
}