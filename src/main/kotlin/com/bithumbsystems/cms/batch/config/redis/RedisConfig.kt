package com.bithumbsystems.cms.batch.config.redis

import com.bithumbsystems.cms.batch.config.aws.ParameterStoreConfig
import com.bithumbsystems.cms.batch.util.PortCheckUtil.findAvailablePort
import com.bithumbsystems.cms.batch.util.PortCheckUtil.isRunning
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import redis.embedded.RedisServer
import java.io.IOException
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
@Profile(value = ["dev", "qa", "perform", "prod", "eks-dev", "eks-prod"])
class RedisConfig {

    @Bean
    fun redissonClient(parameterStoreConfig: ParameterStoreConfig): RedissonClient {
        val config = Config()
        val redisHost = parameterStoreConfig.redisProperties.host
        val redisPort = parameterStoreConfig.redisProperties.port
        config.useClusterServers().nodeAddresses = listOf("rediss://$redisHost:$redisPort")
        parameterStoreConfig.redisProperties.token?.let {
            config.useClusterServers().password = it
        }

        return Redisson.create(config)
    }
}

@Configuration
@Profile(value = ["local", "default", "test"])
class RedisLocalConfig(
    val parameterStoreConfig: ParameterStoreConfig
) {

    private var redisServer: RedisServer? = null
    private val config = Config()

    @PostConstruct
    @Throws(IOException::class)
    fun redisServer() {
        val redisPort =
            if (isRunning(parameterStoreConfig.redisProperties.port)) findAvailablePort()
            else parameterStoreConfig.redisProperties.port

        redisServer = RedisServer.builder()
            .port(redisPort)
            .setting("maxmemory 128M")
            .build()
        redisServer?.start()

        config.useSingleServer().address = "redis://${parameterStoreConfig.redisProperties.host}:$redisPort"
        if (!parameterStoreConfig.redisProperties.token.isNullOrEmpty()) {
            config.useSingleServer().password = parameterStoreConfig.redisProperties.token
        }
    }

    @PreDestroy
    fun stopRedis() {
        redisServer?.stop()
    }

    @Bean
    fun redissonClient(): RedissonClient = Redisson.create(config)
}
