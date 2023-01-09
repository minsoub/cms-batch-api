package com.bithumbsystems.cms.batch.config.redis

import com.bithumbsystems.cms.batch.config.aws.ParameterStoreConfig
import com.bithumbsystems.cms.batch.util.PortCheckUtil.findAvailablePort
import com.bithumbsystems.cms.batch.util.PortCheckUtil.isRunning
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import redis.embedded.RedisServer
import java.io.IOException
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Configuration
@Profile(value = ["dev", "qa", "perform", "prod", "eks-dev", "eks-prod"])
class RedisConfig(
    @Value("\${spring.profiles.active}") val profile: String
) {

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

    @Bean
    fun lockProvider(connectionFactory: RedisConnectionFactory): LockProvider {
        return RedisLockProvider(connectionFactory, profile)
    }
}

@Configuration
@Profile(value = ["local", "default", "test"])
class RedisLocalConfig(
    val parameterStoreConfig: ParameterStoreConfig,
    @Value("\${spring.profiles.active}") val profile: String
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

    @Bean
    fun lockProvider(connectionFactory: RedisConnectionFactory): LockProvider {
        return RedisLockProvider(connectionFactory, profile)
    }
}
