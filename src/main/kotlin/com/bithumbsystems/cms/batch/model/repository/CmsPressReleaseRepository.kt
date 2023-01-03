package com.bithumbsystems.cms.batch.model.repository

import com.bithumbsystems.cms.batch.model.entity.CmsPressRelease
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDateTime

interface CmsPressReleaseRepository : MongoRepository<CmsPressRelease, String> {
    fun findByScheduleDateAfterAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateAsc(now: LocalDateTime)
}
