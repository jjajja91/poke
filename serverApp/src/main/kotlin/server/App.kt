package server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["server", "scan"])
@EntityScan(basePackages = ["server", "scan"])
@EnableJpaRepositories(basePackages = ["server", "scan"])
@ConfigurationPropertiesScan
class App


fun main(args: Array<String>) {
    runApplication<App>(*args)
}