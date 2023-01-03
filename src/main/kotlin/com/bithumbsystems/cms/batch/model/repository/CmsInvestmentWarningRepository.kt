package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsInvestmentWarning
import org.springframework.data.mongodb.repository.MongoRepository

interface CmsInvestmentWarningRepository : MongoRepository<CmsInvestmentWarning, String>
