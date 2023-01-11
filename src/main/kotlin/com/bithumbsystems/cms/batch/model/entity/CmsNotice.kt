package com.bithumbsystems.cms.batch.model.entity

import com.bithumbsystems.cms.batch.config.redis.entity.RedisNotice
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_notice")
class CmsNotice(
    @MongoId
    val id: String = UUID.randomUUID().toString().replace("-", ""),
    var categoryIds: List<String?>?,
    var title: String,
    var content: String,
    val createAccountId: String,
    val createAccountEmail: String,
    val createDate: LocalDateTime = LocalDateTime.now()
) {
    var isFixTop: Boolean = false
    var isShow: Boolean = false
    var isDelete: Boolean = false
    var isBanner: Boolean = false
    var fileId: String? = null
    var shareTitle: String? = null
    var shareDescription: String? = null
    var shareFileId: String? = null
    var shareButtonName: String? = null
    var isSchedule: Boolean = false
    var scheduleDate: LocalDateTime? = null
    var isDraft: Boolean = false
    var readCount: Long = 0
    var isUseUpdateDate: Boolean = false
    var isAlignTop: Boolean = false
    var screenDate: LocalDateTime? = null
    var updateAccountId: String? = null
    var updateAccountEmail: String? = null
    var updateDate: LocalDateTime? = null
}

fun CmsNotice.toRedisEntity(cmsNoticeCategoryMap: Map<String, String>): RedisNotice = RedisNotice(
    id = id,
    title = title,
    categoryNames = categoryIds?.map { categoryId ->
        categoryId?.let {
            cmsNoticeCategoryMap[it]
        } ?: "1"
    }?.toList() ?: listOf(),
    createDate = createDate
)
