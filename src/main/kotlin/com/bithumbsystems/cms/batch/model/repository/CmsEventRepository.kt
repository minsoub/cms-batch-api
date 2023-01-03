package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsEvent
import org.springframework.data.mongodb.repository.MongoRepository

interface CmsEventRepository : MongoRepository<CmsEvent, String>
