package io.hexah.job

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class CheckAuctionHouseDataJob {

    val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000)
    fun run() {
        log.info("Running!")
    }
}
