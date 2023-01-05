package com.bithumbsystems.cms.batch.controller

import com.bithumbsystems.cms.batch.service.BoardService
import com.bithumbsystems.cms.batch.service.NoticeService
import com.bithumbsystems.cms.batch.service.PressReleaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ReservedController(
    private val boardService: BoardService,
    private val noticeService: NoticeService,
    private val pressReleaseService: PressReleaseService
) {

    @GetMapping("notice")
    fun noticeReserveJob(): String =
        noticeService.reservedJob()

    @GetMapping("press-release")
    fun pressReleaseReserveJob(): String =
        pressReleaseService.reservedJob()

    @GetMapping("economic-research")
    fun economicResearchJob(): String =
        boardService.reservedEconomicResearchJob()

    @GetMapping("event")
    fun eventReserveJob(): String =
        boardService.reservedEventJob()

    @GetMapping("investment-warning")
    fun investmentWarningJob(): String =
        boardService.reservedInvestmentWarningJob()

    @GetMapping("review-report")
    fun reviewReportJob(): String =
        boardService.reservedReviewReportJob()
}
