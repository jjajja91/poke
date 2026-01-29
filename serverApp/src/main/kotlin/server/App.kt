package server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["server", "scan"])
@ConfigurationPropertiesScan
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}