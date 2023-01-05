package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsReviewReport
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface CmsReviewReportRepository : MongoRepository<CmsReviewReport, String> {
    fun findByScheduleDateAfterAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateAsc(now: LocalDateTime): List<CmsReviewReport>
    fun findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndFixTopTrueOrderByScreenDateDesc(): List<CmsReviewReport>
}
