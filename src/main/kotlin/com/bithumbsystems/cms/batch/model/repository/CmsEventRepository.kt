package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsEvent
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface CmsEventRepository : MongoRepository<CmsEvent, String> {
    fun findByScheduleDateBeforeAndIsScheduleTrueAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
        now: LocalDateTime
    ): List<CmsEvent>
    fun findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndIsFixTopTrueOrderByScreenDateDesc(): List<CmsEvent>
}
