package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsEconomicResearch
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CmsEconomicResearchRepository : MongoRepository<CmsEconomicResearch, String> {
    fun findByScheduleDateBeforeAndIsScheduleTrueAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
        now: LocalDateTime
    ): List<CmsEconomicResearch>
    fun findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndIsFixTopTrueOrderByScreenDateDesc(): List<CmsEconomicResearch>
}
