package com.bithumbsystems.cms.batch.job

import com.bithumbsystems.cms.batch.service.BoardService
import com.bithumbsystems.cms.batch.service.NoticeService
import com.bithumbsystems.cms.batch.service.PressReleaseService
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.Step
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.job.flow.support.SimpleFlow
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.*

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "30s")
class CmsReservedJobConfig(
    private val noticeService: NoticeService,
    private val boardService: BoardService,
    private val pressReleaseService: PressReleaseService,
    private val jobLauncher: JobLauncher,
    private val jobBuilderFactory: JobBuilderFactory,
    private val stepBuilderFactory: StepBuilderFactory
) {

    companion object {
        private const val JOB_NAME = "CMS_RESERVED_JOB"
    }

    @Bean(name = [JOB_NAME + "_TASK_POOL"])
    fun executor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 128
        executor.setThreadNamePrefix("cms-multi-thread-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.initialize()
        return executor
    }

    @Scheduled(cron = "*/60 * * * * *")
    @SchedulerLock(name = JOB_NAME, lockAtLeastFor = "10s", lockAtMostFor = "50s")
    @Throws(Exception::class)
    fun run() {
        val nowDate = Date()
        val jobParameters = JobParametersBuilder()
            .addDate("nowDate", nowDate)
            .toJobParameters()
        jobLauncher.run(cmsReservedJob(), jobParameters)
    }

    @Bean
    fun cmsReservedJob(): Job =
        jobBuilderFactory.get(JOB_NAME)
            .start(splitFlow())
            .build()
            .build()

    @Bean
    fun splitFlow(): Flow {
        return FlowBuilder<SimpleFlow>("splitFlow")
            .split(executor())
            .add(cmsBoardReservedFlow(), cmsNoticeReservedFlow(), cmsPressReleaseReservedFlow())
            .build()
    }

    @Bean
    fun cmsBoardReservedFlow(): Flow =
        FlowBuilder<SimpleFlow>("cmsBoardReservedFlow")
            .start(cmsReservedInvestmentWarningStep())
            .next(cmsReservedEventStep())
            .next(cmsReservedEconomicResearchStep())
            .next(cmsReservedReviewReportStep())
            .build()

    @Bean
    fun cmsNoticeReservedFlow(): Flow =
        FlowBuilder<SimpleFlow>("cmsNoticeReservedFlow")
            .start(cmsNoticeReservedStep())
            .build()

    @Bean
    fun cmsPressReleaseReservedFlow(): Flow =
        FlowBuilder<SimpleFlow>("cmsPressReleaseReservedFlow")
            .start(cmsPressReleaseReservedStep())
            .build()

    @Bean
    fun cmsNoticeReservedStep(): Step =
        stepBuilderFactory["cmsNoticeReservedStep"]
            .tasklet { _: StepContribution?, _: ChunkContext? ->
                noticeService.reservedJob()
                RepeatStatus.FINISHED
            }
            .build()

    @Bean
    fun cmsPressReleaseReservedStep(): Step =
        stepBuilderFactory["PressRelease"]
            .tasklet { _: StepContribution?, _: ChunkContext? ->
                pressReleaseService.reservedJob()
                RepeatStatus.FINISHED
            }
            .build()

    @Bean
    fun cmsReservedInvestmentWarningStep(): Step {
        return stepBuilderFactory["InvestmentWarning"]
            .tasklet { _: StepContribution?, _: ChunkContext? ->
                boardService.reservedInvestmentWarningJob()
                RepeatStatus.FINISHED
            }
            .build()
    }

    @Bean
    fun cmsReservedEventStep(): Step {
        return stepBuilderFactory["Event"]
            .tasklet { _: StepContribution?, _: ChunkContext? ->
                boardService.reservedEventJob()
                RepeatStatus.FINISHED
            }
            .build()
    }

    @Bean
    fun cmsReservedEconomicResearchStep(): Step {
        return stepBuilderFactory["EconomicResearch"]
            .tasklet { _: StepContribution?, _: ChunkContext? ->
                boardService.reservedEconomicResearchJob()
                RepeatStatus.FINISHED
            }
            .build()
    }

    @Bean
    fun cmsReservedReviewReportStep(): Step {
        return stepBuilderFactory["ReviewReport"]
            .tasklet { _: StepContribution?, _: ChunkContext? ->
                boardService.reservedReviewReportJob()
                RepeatStatus.FINISHED
            }
            .build()
    }
}
