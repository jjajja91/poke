package scan.tx

import kotlinx.coroutines.runBlocking
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate

@Profile("jpa")
@Component
class TxRunnerJpa(
    private val txTemplate: TransactionTemplate
) : TxRunner {
    override suspend fun <T> tx(block: suspend () -> T): T = requireNotNull(txTemplate.execute { runBlocking { block() } }) { "tx returned null" }
}