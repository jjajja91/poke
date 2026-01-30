package scan.tx

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait

@Profile("r2dbc")
@Component
class TxRunnerR2dbc(
    private val op: TransactionalOperator
) : TxRunner {
    override suspend fun <T> tx(block: suspend () -> T): T = requireNotNull(op.executeAndAwait { block() }) { "tx returned null" }
}