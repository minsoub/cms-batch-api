package com.bithumbsystems.cms.batch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class],
    scanBasePackages = [
        "com.bithumbsystems.cms",
        "org.springframework.batch.core.configuration.annotation"
    ]
)
@EnableBatchProcessing
@EnableScheduling
@ConfigurationPropertiesScan("com.bithumbsystems.cms.batch.config")
class CmsBatchApiApplication

fun main(args: Array<String>) {
    runApplication<CmsBatchApiApplication>(*args)
}
