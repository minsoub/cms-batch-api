package com.bithumbsystems.cms.batch.config.mongo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "spring.data.mongodb")
class MongoProperties(
    val uri: String,
    val username: String,
    val password: String,
    val port: String,
    val database: String
)
