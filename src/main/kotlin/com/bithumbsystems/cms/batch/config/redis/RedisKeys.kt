package com.bithumbsystems.cms.batch.config.redis

enum class RedisKeys(val description: String) {
    CMS_NOTICE_FIX("공지사항 고정 게시글"),

    CMS_PRESS_RELEASE_FIX("보도자료 고정 게시글"),

    CMS_EVENT_FIX("이벤트 고정 게시글"),

    CMS_REVIEW_REPORT_FIX("가상 자산 고정 게시글"),

    CMS_INVESTMENT_WARNING_FIX("투자유의 고정 게시글"),

    CMS_ECONOMIC_RESEARCH_FIX("경제연구소 고정 게시글"),

    CMS_NOTICE_RECENT("메인 페이지 공지사항"),

    CMS_PRESS_RELEASE_RECENT("메인페이지 보도자료"),

    CMS_NOTICE_BANNER("공지사항 배너")
}

enum class RedisReadCount {
    CMS_NOTICE_READ_COUNT,
    CMS_PRESS_RELEASE_READ_COUNT,
    CMS_EVENT_READ_COUNT,
    CMS_REVIEW_REPORT_READ_COUNT,
    CMS_INVESTMENT_WARNING_READ_COUNT,
    CMS_ECONOMIC_RESEARCH_READ_COUNT
}
