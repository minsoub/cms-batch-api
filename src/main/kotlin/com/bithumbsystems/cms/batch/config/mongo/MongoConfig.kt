package com.bithumbsystems.cms.batch.config.mongo

import com.bithumbsystems.cms.batch.config.aws.ParameterStoreConfig
import com.bithumbsystems.cms.batch.util.Logger
import com.mongodb.ConnectionString
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.convert.NoOpDbRefResolver
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories

@Configuration
@EnableMongoRepositories("com.bithumbsystems.cms.batch.model")
class MongoConfig(
    private val parameterStoreConfig: ParameterStoreConfig
) : AbstractMongoClientConfiguration() {
    private val logger by Logger()

    override fun getDatabaseName() = parameterStoreConfig.mongoProperties.database

    @Bean
    override fun mongoDbFactory(): MongoDatabaseFactory {
        return SimpleMongoClientDatabaseFactory(getConnectionString(parameterStoreConfig.mongoProperties))
    }

    @Bean
    override fun mongoTemplate(
        databaseFactory: MongoDatabaseFactory,
        mongoConverter: MappingMongoConverter
    ): MongoTemplate = MongoTemplate(databaseFactory, mongoConverter)

    private fun getConnectionString(mongoProperties: MongoProperties): ConnectionString {
        logger.info(
            "mongodb://${mongoProperties.username}:${mongoProperties.password}" +
                "@${mongoProperties.uri}:${mongoProperties.port}/$databaseName?authSource=$databaseName"
        )
        return ConnectionString(
            "mongodb://${mongoProperties.username}:${mongoProperties.password}" +
                "@${mongoProperties.uri}:${mongoProperties.port}/$databaseName?authSource=$databaseName"
        )
    }

    @Bean
    @Primary
    override fun mappingMongoConverter(
        databaseFactory: MongoDatabaseFactory,
        customConversions: MongoCustomConversions,
        mappingContext: MongoMappingContext
    ): MappingMongoConverter {
        mappingContext.setFieldNamingStrategy(SnakeCaseFieldNamingStrategy())
        mappingContext.isAutoIndexCreation = true
        val converter = MappingMongoConverter(NoOpDbRefResolver.INSTANCE, mappingContext)
        converter.setCustomConversions(customConversions)
        converter.setCodecRegistryProvider(databaseFactory)
        converter.setTypeMapper(DefaultMongoTypeMapper(null))
        return converter
    }

    @Bean
    fun transactionManager(factory: MongoDatabaseFactory): MongoTransactionManager {
        return MongoTransactionManager(factory)
    }
}
