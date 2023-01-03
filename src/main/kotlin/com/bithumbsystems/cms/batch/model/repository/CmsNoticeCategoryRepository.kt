package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsNoticeCategory
import org.springframework.data.mongodb.repository.MongoRepository

interface CmsNoticeCategoryRepository : MongoRepository<CmsNoticeCategory, String> {

    fun findByIsUseTrueAndIsDeleteFalse(): List<CmsNoticeCategory>
}
