package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsNotice
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CmsNoticeRepository : MongoRepository<CmsNotice, String> {
    fun findByScheduleDateBeforeAndIsScheduleTrueAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
        now: LocalDateTime
    ): List<CmsNotice>

    fun findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndIsFixTopTrueOrderByScreenDateDesc(): List<CmsNotice>

    fun findFirst5ByIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(): List<CmsNotice>

    fun findByIsBannerTrueAndScheduleDateBefore(now: LocalDateTime): List<CmsNotice>
}
