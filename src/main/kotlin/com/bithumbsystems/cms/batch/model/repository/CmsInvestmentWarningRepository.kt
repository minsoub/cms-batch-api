package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsInvestmentWarning
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface CmsInvestmentWarningRepository : MongoRepository<CmsInvestmentWarning, String> {
    fun findByScheduleDateAfterAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateAsc(now: LocalDateTime): List<CmsInvestmentWarning>
    fun findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndFixTopTrueOrderByScreenDateDesc(): List<CmsInvestmentWarning>
}
