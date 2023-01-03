package com.bithumbsystems.cms.batch.config.aws

import com.bithumbsystems.cms.batch.config.mongo.MongoProperties
import com.bithumbsystems.cms.batch.config.redis.RedisProperties
import com.bithumbsystems.cms.batch.util.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import java.net.URI

@Configuration
@Primary
class ParameterStoreConfig(
    awsProperties: AwsProperties,
    parameterStoreProperties: ParameterStoreProperties,
    localMongoProperties: MongoProperties,
    localRedisProperties: RedisProperties,
    @Value("\${spring.profiles.active}") profile: String
) {
    private val logger by Logger()

    private val isLocalOrDefault = profile == "local" || profile == "default" || profile == "test"
    private val profileName = if (isLocalOrDefault) "local" else awsProperties.profileName
    lateinit var ssmClient: SsmClient
    lateinit var mongoProperties: MongoProperties
    lateinit var redisProperties: RedisProperties

    init {
        if (isLocalOrDefault) {
            mongoProperties = localMongoProperties
            redisProperties = localRedisProperties
            ssmClient = SsmClient.builder().credentialsProvider(
                ProfileCredentialsProvider.create(awsProperties.profileName)
            ).endpointOverride(URI.create(awsProperties.ssmEndPoint)).region(
                Region.of(awsProperties.region)
            ).build()
        } else {
            mongoProperties = MongoProperties(
                getParameterValue(
                    parameterStoreProperties.prefix,
                    parameterStoreProperties.docName,
                    ParameterStoreCode.DB_URL.value
                ),
                getParameterValue(
                    parameterStoreProperties.prefix,
                    parameterStoreProperties.docName,
                    ParameterStoreCode.DB_USER.value
                ),
                getParameterValue(
                    parameterStoreProperties.prefix,
                    parameterStoreProperties.docName,
                    ParameterStoreCode.DB_PASSWORD.value
                ),
                getParameterValue(
                    parameterStoreProperties.prefix,
                    parameterStoreProperties.docName,
                    ParameterStoreCode.DB_PORT.value
                ),
                getParameterValue(
                    parameterStoreProperties.prefix,
                    parameterStoreProperties.docName,
                    ParameterStoreCode.DB_NAME.value
                )
            )

            redisProperties = RedisProperties(
                getParameterValue(
                    parameterStoreProperties.prefix,
                    parameterStoreProperties.redisName,
                    ParameterStoreCode.REDIS_HOST.value
                ),
                getParameterValue(
                    parameterStoreProperties.prefix,
                    parameterStoreProperties.redisName,
                    ParameterStoreCode.REDIS_PORT.value
                ).toInt(),
                getParameterValue(
                    parameterStoreProperties.prefix,
                    parameterStoreProperties.redisName,
                    ParameterStoreCode.REDIS_TOKEN.value
                )
            )
            awsProperties.kmsKey =
                getParameterValue(
                    parameterStoreProperties.smartPrefix,
                    parameterStoreProperties.kmsName,
                    ParameterStoreCode.KMS_ALIAS_NAME.value
                )
            ssmClient = SsmClient.builder().endpointOverride(URI.create(awsProperties.ssmEndPoint))
                .region(Region.of(awsProperties.region)).build()
        }
    }

    private final fun getParameterValue(
        prefix: String,
        storeName: String,
        type: String
    ): String {
        logger.info("getParameter: $prefix/${storeName}_$profileName/$type")
        return ssmClient.getParameter(
            GetParameterRequest.builder().name("$prefix/${storeName}_$profileName/$type").withDecryption(true).build()
        ).parameter().value()
    }
}
