package com.bithumbsystems.cms.batch.config.redis.listener

import com.bithumbsystems.cms.batch.config.redis.RedisReadCount
import com.bithumbsystems.cms.batch.model.entity.*
import com.bithumbsystems.cms.batch.util.Logger
import org.redisson.api.RPatternTopic
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component

@Component
class ReadCountListener(
    private val redissonClient: RedissonClient,
    private val mongoTemplate: MongoTemplate
) {

    private val logger by Logger()

    companion object {
        const val BULK_UPDATE_COUNT = 10
    }

    @Bean
    fun topicNoticeListener() {
        val topic: RPatternTopic = redissonClient.getPatternTopic("*_READ_COUNT_TOPIC")
        topicAddListener(topic)
    }

    private fun topicAddListener(topic: RPatternTopic) {
        topic.addListener(String::class.java) { pattern: CharSequence, channel: CharSequence, msg: String ->
            logger.info("subscribe -> Pattern: $pattern Channel: $channel, Message: $msg")

            val target = when (RedisReadCount.valueOf(msg)) {
                RedisReadCount.CMS_NOTICE_READ_COUNT -> CmsNotice::class.java
                RedisReadCount.CMS_PRESS_RELEASE_READ_COUNT -> CmsPressRelease::class.java
                RedisReadCount.CMS_EVENT_READ_COUNT -> CmsEvent::class.java
                RedisReadCount.CMS_REVIEW_REPORT_READ_COUNT -> CmsReviewReport::class.java
                RedisReadCount.CMS_INVESTMENT_WARNING_READ_COUNT -> CmsInvestmentWarning::class.java
                RedisReadCount.CMS_ECONOMIC_RESEARCH_READ_COUNT -> CmsEconomicResearch::class.java
            }
            val readCountQueue = redissonClient.getDeque<String>(msg)
            if (readCountQueue.count() > BULK_UPDATE_COUNT) {
                val updateMap = mutableMapOf<String, Long> ()
                (0 until readCountQueue.count() step 1).map {
                    val id: String? = readCountQueue.pollLast()
                    id?.let { key -> updateMap.merge(key, 1) { before, value -> before + value } }
                }
                logger.info("Update -> $updateMap")
                updateMap.map {
                    val query = Query().addCriteria(Criteria.where("_id").`is`(it.key))
                    val update = Update().inc("read_count", it.value)
                    mongoTemplate.updateFirst(query, update, target)
                }
            }
        }
    }
}
