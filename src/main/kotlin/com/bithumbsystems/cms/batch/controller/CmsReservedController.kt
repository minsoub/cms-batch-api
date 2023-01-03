package com.bithumbsystems.cms.batch.controller

import com.bithumbsystems.cms.batch.service.NoticeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CmsReservedController(
    private val noticeService: NoticeService
) {

    @GetMapping("notice")
    fun noticeReserveJob(): String =
        noticeService.reservedJob()
}
