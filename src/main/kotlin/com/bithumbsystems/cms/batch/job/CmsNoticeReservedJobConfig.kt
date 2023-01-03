package com.bithumbsystems.cms.batch.job

import com.bithumbsystems.cms.batch.item.CmsNoticeReservedTasklet
import com.bithumbsystems.cms.batch.service.NoticeService
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.*

@Configuration
class CmsNoticeReservedJobConfig(
    private val noticeService: NoticeService,
    private val jobLauncher: JobLauncher,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    companion object {
        private const val JOB_NAME = "CMS_NOTICE_RESERVED_JOB"
        private const val JOB_STEP = JOB_NAME + "_STEP"
    }

    @Scheduled(cron = "*/60 * * * * *")
    @Throws(Exception::class)
    fun run() {
        val nowDate = Date()
        val jobParameters = JobParametersBuilder()
            .addDate("nowDate", nowDate)
            .toJobParameters()
        jobLauncher.run(cmsNoticeReservedJob(), jobParameters)
    }

    @Bean
    fun cmsNoticeReservedJob(): Job =
        jobBuilderFactory.get(JOB_NAME)
            .start(cmsNoticeReservedStep())
            .build()

    @Bean
    fun cmsNoticeReservedStep(): Step =
        stepBuilderFactory.get(JOB_STEP)
            .tasklet(
                CmsNoticeReservedTasklet(noticeService)
            )
            .build()
}
