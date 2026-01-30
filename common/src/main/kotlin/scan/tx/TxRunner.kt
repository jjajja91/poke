package scan.tx

interface TxRunner {
    suspend fun <T> tx(block: suspend () -> T): T
}