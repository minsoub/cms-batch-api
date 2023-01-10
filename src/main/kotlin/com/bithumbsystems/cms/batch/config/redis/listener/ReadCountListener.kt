package com.bithumbsystems.cms.batch.config.redis.listener

import com.bithumbsystems.cms.batch.config.redis.RedisKeys
import com.bithumbsystems.cms.batch.model.entity.*
import com.bithumbsystems.cms.batch.util.Logger
import org.redisson.api.RTopic
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

    @Bean
    fun topicNoticeListener() {
        val topic: RTopic = redissonClient.getTopic("${RedisKeys.CMS_NOTICE_FIX}_TOPIC")
        topicAddListener(topic, CmsNotice::class.java)
    }

    @Bean
    fun topicEconomicResearchListener() {
        val topic: RTopic = redissonClient.getTopic("${RedisKeys.CMS_ECONOMIC_RESEARCH_FIX}_TOPIC")
        topicAddListener(topic, CmsEconomicResearch::class.java)
    }

    @Bean
    fun topicInvestmentWarningListener() {
        val topic: RTopic = redissonClient.getTopic("${RedisKeys.CMS_INVESTMENT_WARNING_FIX}_TOPIC")
        topicAddListener(topic, CmsInvestmentWarning::class.java)
    }

    @Bean
    fun topicEventListener() {
        val topic: RTopic = redissonClient.getTopic("${RedisKeys.CMS_EVENT_FIX}_TOPIC")
        topicAddListener(topic, CmsEvent::class.java)
    }

    @Bean
    fun topicReviewReportListener() {
        val topic: RTopic = redissonClient.getTopic("${RedisKeys.CMS_REVIEW_REPORT_FIX}_TOPIC")
        topicAddListener(topic, CmsReviewReport::class.java)
    }

    private fun <T> topicAddListener(topic: RTopic, target: Class<T>) {
        topic.addListener(String::class.java) { charSequence: CharSequence, id: String ->
            logger.info("subscribe -> Channel: $charSequence, Message: $id")
            val query = Query().addCriteria(Criteria.where("_id").`is`(id))
            val update = Update().inc("read_count", 1)
            mongoTemplate.updateFirst(query, update, target)
        }
    }
}
