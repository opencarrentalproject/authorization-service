package com.opencarrental.enduserservice

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer

@SpringBootTest
@ContextConfiguration(initializers = [AbstractIntegrationTest.Initializer::class])
abstract class AbstractIntegrationTest {

    companion object {
        val mongodbContainter = GenericContainer<Nothing>("mongo:4.0")
                .apply { withExposedPorts(27017) }
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            mongodbContainter.start()

            TestPropertyValues.of(
                    "spring.data.mongodb.host=${mongodbContainter.containerIpAddress}",
                    "spring.data.mongodb.port=${mongodbContainter.firstMappedPort}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}