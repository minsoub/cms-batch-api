package com.bithumbsystems.cms.batch.config.redis.entity

import java.time.LocalDateTime

data class RedisBoard(
    val id: String,
    val title: String,
    val createDate: LocalDateTime
)
