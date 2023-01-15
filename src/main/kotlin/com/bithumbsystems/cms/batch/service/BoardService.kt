package com.bithumbsystems.cms.batch.service

import com.bithumbsystems.cms.batch.config.aws.AwsProperties
import com.bithumbsystems.cms.batch.config.redis.RedisKeys
import com.bithumbsystems.cms.batch.config.redis.RedisRepository
import com.bithumbsystems.cms.batch.config.redis.entity.RedisBoard
import com.bithumbsystems.cms.batch.config.redis.entity.RedisThumbnail
import com.bithumbsystems.cms.batch.model.entity.CmsInvestmentWarning
import com.bithumbsystems.cms.batch.model.entity.toRedisEntity
import com.bithumbsystems.cms.batch.model.repository.CmsEconomicResearchRepository
import com.bithumbsystems.cms.batch.model.repository.CmsEventRepository
import com.bithumbsystems.cms.batch.model.repository.CmsInvestmentWarningRepository
import com.bithumbsystems.cms.batch.model.repository.CmsReviewReportRepository
import com.bithumbsystems.cms.batch.util.Logger
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BoardService(
    private val cmsEconomicResearchRepository: CmsEconomicResearchRepository,
    private val cmsEventRepository: CmsEventRepository,
    private val cmsInvestmentWarningRepository: CmsInvestmentWarningRepository,
    private val cmsReviewReportRepository: CmsReviewReportRepository,
    private val redisRepository: RedisRepository,
    awsProperties: AwsProperties
) {

    private val logger by Logger()

    companion object {
        var s3Url: String = ""
    }

    init {
        s3Url = "https://${awsProperties.bucket}.s3.${awsProperties.region}.amazonaws.com"
    }

    @Transactional
    fun reservedReviewReportJob(): String {
        val targetList =
            cmsReviewReportRepository.findByScheduleDateBeforeAndIsScheduleTrueAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
                now = LocalDateTime.now()
            )
        val targetCount = targetList.count()
        var fixTopList = 0
        logger.info("[CmsBoardReserved] START : count: $targetCount")

        var hasIsFixTop = false

        targetList.map {
            hasIsFixTop = it.isFixTop
            it.isShow = true
            it.isSchedule = false
            it.screenDate = it.scheduleDate
            cmsReviewReportRepository.save(it)
        }

        if (hasIsFixTop) {
            fixTopList = saveRedisFixReviewReport()
        }
        logger.info("[CmsBoardReserved] END")

        return "targetCount: $targetCount, fixTopListCount: $fixTopList"
    }

    private fun saveRedisFixReviewReport(): Int {
        logger.info("[CmsBoardReserved][saveRedisFixInvestmentWarning] START")
        val fixTopList = cmsReviewReportRepository.findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndIsFixTopTrueOrderByScreenDateDesc()

        fixTopList.map { item -> item.toRedisEntity() }.also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = RedisKeys.CMS_REVIEW_REPORT_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisThumbnail>>() {}
            )
        }
        logger.info("[CmsBoardReserved][saveRedisFixInvestmentWarning] END")
        return fixTopList.count()
    }

    @Transactional
    fun reservedInvestmentWarningJob(): String {
        val fixTop = cmsInvestmentWarningRepository
            .findFirstByScheduleDateBeforeAndIsScheduleTrueAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
                now = LocalDateTime.now()
            )
        logger.info("[CmsBoardReserved] START")

        fixTop?.apply {
            this.isShow = true
            this.isSchedule = false
            this.screenDate = this.scheduleDate
            cmsInvestmentWarningRepository.save(this)
            saveRedisFixInvestmentWarning(this)
        }

        logger.info("[CmsBoardReserved] END")

        return "fixTop: ${fixTop?.id}"
    }

    private fun saveRedisFixInvestmentWarning(fixTop: CmsInvestmentWarning) {
        logger.info("[CmsBoardReserved][saveRedisFixInvestmentWarning] START")

        fixTop.toRedisEntity().also {
            redisRepository.addOrUpdateRBucket(
                bucketKey = RedisKeys.CMS_INVESTMENT_WARNING_FIX,
                value = it.id,
                typeReference = object : TypeReference<String>() {}
            )
        }
        logger.info("[CmsBoardReserved][saveRedisFixInvestmentWarning] END")
    }

    @Transactional
    fun reservedEventJob(): String {
        val targetList = cmsEventRepository
            .findByScheduleDateBeforeAndIsScheduleTrueAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
                now = LocalDateTime.now()
            )
        val targetCount = targetList.count()
        var fixTopList = 0
        logger.info("[CmsBoardReserved] START : count: $targetCount")

        var hasIsFixTop = false

        targetList.map {
            hasIsFixTop = it.isFixTop
            it.isShow = true
            it.isSchedule = false
            it.screenDate = it.scheduleDate
            cmsEventRepository.save(it)
        }

        if (hasIsFixTop) {
            fixTopList = saveRedisFixEvent()
        }
        logger.info("[CmsBoardReserved] END")

        return "targetCount: $targetCount, fixTopListCount: $fixTopList"
    }

    private fun saveRedisFixEvent(): Int {
        logger.info("[CmsBoardReserved][saveRedisFixEconomicResearch] START")
        val fixTopList = cmsEventRepository.findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndIsFixTopTrueOrderByScreenDateDesc()

        fixTopList.map { item -> item.toRedisEntity() }.also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = RedisKeys.CMS_EVENT_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisBoard>>() {}
            )
        }
        logger.info("[CmsBoardReserved][saveRedisFixEconomicResearch] END")
        return fixTopList.count()
    }

    @Transactional
    fun reservedEconomicResearchJob(): String {
        val targetList = cmsEconomicResearchRepository
            .findByScheduleDateBeforeAndIsScheduleTrueAndIsShowTrueAndIsDeleteFalseAndIsDraftFalseOrderByScreenDateDesc(
                now = LocalDateTime.now()
            )
        val targetCount = targetList.count()
        var fixTopList = 0
        logger.info("[CmsBoardReserved] START : count: $targetCount")

        var hasIsFixTop = false

        targetList.map {
            hasIsFixTop = it.isFixTop
            it.isShow = true
            it.isSchedule = false
            it.screenDate = it.scheduleDate
            cmsEconomicResearchRepository.save(it)
        }

        if (hasIsFixTop) {
            fixTopList = saveRedisFixEconomicResearch()
        }
        logger.info("[CmsBoardReserved] END")

        return "targetCount: $targetCount, fixTopListCount: $fixTopList"
    }

    private fun saveRedisFixEconomicResearch(): Int {
        logger.info("[CmsBoardReserved][saveRedisFixEconomicResearch] START")
        val fixTopList = cmsEconomicResearchRepository.findByIsShowTrueAndIsDeleteFalseAndIsDraftFalseAndIsFixTopTrueOrderByScreenDateDesc()

        fixTopList.map { item -> item.toRedisEntity() }.also { totalList ->
            redisRepository.addOrUpdateRBucket(
                bucketKey = RedisKeys.CMS_ECONOMIC_RESEARCH_FIX,
                value = totalList,
                typeReference = object : TypeReference<List<RedisThumbnail>>() {}
            )
        }
        logger.info("[CmsBoardReserved][saveRedisFixEconomicResearch] END")
        return fixTopList.count()
    }
}
