package com.bithumbsystems.cms.batch.config.aws

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@Configuration
class AwsProperties(
    @Value("\${cloud.aws.credentials.profile-name}") val profileName: String,
    @Value("\${cloud.aws.region.static}") val region: String,
    @Value("\${cloud.aws.s3.bucket}") val bucket: String,
    @Value("\${cloud.aws.ssm.endpoint}") val ssmEndPoint: String,
    @Value("\${cloud.aws.kms.endpoint}") val kmsEndPoint: String
) {
    lateinit var kmsKey: String
}

@ConstructorBinding
@ConfigurationProperties(prefix = "cloud.aws.param-store")
class ParameterStoreProperties(
    val prefix: String,
    val smartPrefix: String,
    val docName: String,
    val kmsName: String,
    val redisName: String
)
