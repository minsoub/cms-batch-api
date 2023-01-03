package com.bithumbsystems.cms.batch.model.entity

import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.MongoId
import java.time.LocalDateTime
import java.util.*

@Document("cms_notice_category")
class CmsNoticeCategory(
    @MongoId
    val id: String = UUID.randomUUID().toString().replace("-", ""),
    var name: String,
    var isUse: Boolean = true,
    var isDelete: Boolean = false,
    val createAccountId: String,
    val createAccountEmail: String,
    val createDate: LocalDateTime = LocalDateTime.now()
) {
    var updateAccountId: String? = null
    var updateAccountEmail: String? = null
    var updateDate: LocalDateTime? = null
}
