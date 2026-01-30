package scan.batch.service

import scan.enum.EnumFailDomain
import scan.util.coroutine.BatchResult

interface BatchStrategy<V> {
    val domain: EnumFailDomain
    suspend fun getIdSet(): Set<Int>
    suspend fun fetchData(idSet: Set<Int>): BatchResult<Int, V>
    suspend fun saveData(list: List<V>)
    suspend fun clearData()
}