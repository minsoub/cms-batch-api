package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsReviewReport
import org.springframework.data.mongodb.repository.MongoRepository

interface CmsReviewReportRepository : MongoRepository<CmsReviewReport, String>
