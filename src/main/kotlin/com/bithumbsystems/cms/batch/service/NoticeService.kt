package com.bithumbsystems.cms.batch.service

import com.bithumbsystems.cms.batch.config.redis.RedisKeys
import com.bithumbsystems.cms.batch.config.redis.RedisRepository
import com.bithumbsystems.cms.batch.config.redis.entity.RedisNotice
import com.bithumbsystems.cms.batch.model.entity.CmsNotice
import com.bithumbsystems.cms.batch.model.entity.toRedisEntity
import com.bithumbsystems.cms.batch.model.repository.CmsNoticeCategoryRepository
import com.bithumbsystems.cms.batch.model.repository.CmsNoticeRepository
import com.bithumbsystems.cms.batch.util.Logger
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NoticeService(
    private val cmsNoticeCategoryRepository: CmsNoticeCategoryRepository,
    private val cmsNoticeRepository: CmsNoticeRepository,
    private val redisRepository: RedisRepository
) {

    private val logger by Logger()

    @Transactional
    fun reservedJob(): String {
        val cmsNoticeScheduleList = cmsNoticeRepository.findByScheduleDateAfterAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
            now = LocalDateTime.now()
        )
        val cmsNoticeCategoryMap = cmsNoticeCategoryRepository.findByIsUseTrueAndIsDeleteFalse().associate {
            it.id to it.name
        }
        val targetCount = cmsNoticeScheduleList.count()
        var noticeBannerId = ""
        var newListCount = 0
        var fixTopList = 0
        logger.info("[CmsNoticeReserved] START : count: $targetCount")

        var hasIsFixTop = false

        cmsNoticeScheduleList.map { cmsNotice ->
            hasIsFixTop = cmsNotice.isFixTop
            cmsNotice.isShow = true
            cmsNotice.screenDate = cmsNotice.scheduleDate
            if (cmsNotice.isBanner) {
                noticeBannerId = saveRedisNoticeBanner(cmsNoticeCategoryMap, cmsNotice)
            }
            newListCount = saveRedisMainNotice(cmsNoticeCategoryMap)
            cmsNoticeRepository.save(cmsNotice)
        }

        if (hasIsFixTop) {
            fixTopList = saveRedisFixNotice(cmsNoticeCategoryMap)
        }
        logger.info("[CmsNoticeReserved] END")

        return "targetCount: $targetCount, noticeBannerId: $noticeBannerId, newListCount: $newListCount, fixTopListCount: $fixTopList"
    }

    private fun saveRedisMainNotice(
        cmsNoticeCategoryMap: Map<String, String>
    ): Int {
        logger.info("[CmsNoticeReserved][saveRedisMainNotice] START")
        val newList = cmsNoticeRepository.findFirst5ByIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc()

        newList.map { it.toRedisEntity(cmsNoticeCategoryMap) }.toList().also { topList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = RedisKeys.CMS_NOTICE_RECENT,
                value = topList,
                typeReference = object : TypeReference<List<RedisNotice>>() {}
            )
        }
        logger.info("[CmsNoticeReserved][saveRedisMainNotice] END")
        return newList.count()
    }

    private fun saveRedisNoticeBanner(
        cmsNoticeCategoryMap: Map<String, String>,
        cmsNotice: CmsNotice
    ): String {
        logger.info("[CmsNoticeReserved][saveRedisNoticeBanner] START")
        redisRepository.addOrUpdateRBucket(
            bucketKey = RedisKeys.CMS_NOTICE_BANNER,
            value = cmsNotice.toRedisEntity(cmsNoticeCategoryMap),
            typeReference = object : TypeReference<RedisNotice>() {}
        )
        cmsNoticeRepository.saveAll(
            cmsNoticeRepository.findByIsBannerTrueAndScheduleDateBefore(now = cmsNotice.screenDate ?: cmsNotice.createDate).map {
                it.isBanner = false
                it
            }
        )
        logger.info("[CmsNoticeReserved][saveRedisNoticeBanner] END")

        return cmsNotice.id
    }

    private fun saveRedisFixNotice(cmsNoticeCategoryMap: Map<String, String>): Int {
        logger.info("[CmsNoticeReserved][saveRedisFixNotice] START")
        val fixTopList = cmsNoticeRepository.findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndFixTopTrueOrderByScreenDateDesc()

        fixTopList.map { item -> item.toRedisEntity(cmsNoticeCategoryMap) }.toList().also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = RedisKeys.CMS_NOTICE_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisNotice>>() {}
            )
        }
        logger.info("[CmsNoticeReserved][saveRedisFixNotice] END")
        return fixTopList.count()
    }
}
