package com.bithumbsystems.cms.batch.service

import com.bithumbsystems.cms.batch.config.redis.RedisKeys
import com.bithumbsystems.cms.batch.config.redis.RedisRepository
import com.bithumbsystems.cms.batch.config.redis.entity.RedisBoard
import com.bithumbsystems.cms.batch.model.entity.toRedisEntity
import com.bithumbsystems.cms.batch.model.repository.CmsPressReleaseRepository
import com.bithumbsystems.cms.batch.util.Logger
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PressReleaseService(
    private val cmsPressReleaseRepository: CmsPressReleaseRepository,
    private val redisRepository: RedisRepository
) {

    private val logger by Logger()

    @Transactional
    fun reservedJob(): String {
        val releaseReservedList = cmsPressReleaseRepository.findByScheduleDateAfterAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
            now = LocalDateTime.now()
        )

        val targetCount = releaseReservedList.count()
        var newListCount = 0
        var fixTopList = 0
        logger.info("[PressReleaseReserved] START : count: $targetCount")

        var hasIsFixTop = false

        releaseReservedList.map { cmsPressRelease ->
            hasIsFixTop = cmsPressRelease.isFixTop
            cmsPressRelease.isShow = true
            cmsPressRelease.screenDate = cmsPressRelease.scheduleDate
            newListCount = saveRedisMainPressRelease()
            cmsPressReleaseRepository.save(cmsPressRelease)
        }

        if (hasIsFixTop) {
            fixTopList = saveRedisFixPressRelease()
        }
        logger.info("[PressReleaseReserved] END")

        return "targetCount: $targetCount, newListCount: $newListCount, fixTopListCount: $fixTopList"
    }

    private fun saveRedisMainPressRelease(): Int {
        logger.info("[PressReleaseReserved][saveRedisMainPressRelease] START")
        val newList = cmsPressReleaseRepository.findFirst5ByIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc()

        newList.map { it.toRedisEntity() }.toList().also { topList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = RedisKeys.CMS_PRESS_RELEASE_RECENT,
                value = topList,
                typeReference = object : TypeReference<List<RedisBoard>>() {}
            )
        }
        logger.info("[PressReleaseReserved][saveRedisMainPressRelease] END")
        return newList.count()
    }

    private fun saveRedisFixPressRelease(): Int {
        logger.info("[PressReleaseReserved][saveRedisFixPressRelease] START")
        val fixTopList = cmsPressReleaseRepository.findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndFixTopTrueOrderByScreenDateDesc()

        fixTopList.map { item -> item.toRedisEntity() }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = RedisKeys.CMS_PRESS_RELEASE_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisBoard>>() {}
            )
        }
        logger.info("[PressReleaseReserved][saveRedisFixPressRelease] END")
        return fixTopList.count()
    }
}
