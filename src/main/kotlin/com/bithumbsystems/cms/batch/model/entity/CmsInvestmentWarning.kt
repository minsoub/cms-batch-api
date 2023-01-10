package com.bithumbsystems.cms.batch.model.entity

import com.bithumbsystems.cms.batch.config.redis.entity.RedisBoard
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime

@Document("cms_investment_warning")
class CmsInvestmentWarning(
    @MongoId
    val id: String,
    val title: String,
    val isFixTop: Boolean = false,
    var isShow: Boolean = true,
    val isDelete: Boolean = false,
    val content: String,
    val fileId: String? = null,
    val shareTitle: String? = null,
    val shareDescription: String? = null,
    val shareFileId: String? = null,
    val shareButtonName: String? = null,
    val isSchedule: Boolean = false,
    val scheduleDate: LocalDateTime? = null,
    val isDraft: Boolean = false,
    val readCount: Long = 0,
    val createAccountId: String,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val updateAccountId: String? = null,
    val updateDate: LocalDateTime? = null,
    val isUseUpdateDate: Boolean = false,
    val isAlignTop: Boolean = false,
    var screenDate: LocalDateTime?
)

fun CmsInvestmentWarning.toRedisEntity(): RedisBoard = RedisBoard(
    id = id,
    title = title,
    createDate = createDate
)
