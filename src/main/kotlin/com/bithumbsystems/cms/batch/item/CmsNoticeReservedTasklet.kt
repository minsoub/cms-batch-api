package com.bithumbsystems.cms.batch.item

import com.bithumbsystems.cms.batch.service.NoticeService
import org.springframework.batch.core.StepContribution
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.tasklet.Tasklet
import org.springframework.batch.repeat.RepeatStatus

class CmsNoticeReservedTasklet(
    private val noticeService: NoticeService
) : Tasklet {

    override fun execute(contribution: StepContribution, chunkContext: ChunkContext): RepeatStatus {
        noticeService.reservedJob()
        return RepeatStatus.FINISHED
    }
}
