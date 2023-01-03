package com.bithumbsystems.cms.batch.config.redis.entity

import java.time.LocalDateTime

data class RedisThumbnail(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val screenDate: LocalDateTime
)
