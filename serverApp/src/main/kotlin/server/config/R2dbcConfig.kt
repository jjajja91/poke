package server.config

import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@Profile("r2dbc")
class R2dbcConfig {

    @Bean
    fun databaseClient(cf: ConnectionFactory): DatabaseClient =
        DatabaseClient.create(cf)

    @Bean
    fun transactionalOperator(cf: ConnectionFactory): TransactionalOperator =
        TransactionalOperator.create(R2dbcTransactionManager(cf))
}