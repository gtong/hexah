package io.hexah.job

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import io.hexah.dao.AuctionHouseFeedDao
import io.hexah.model.AuctionHouseFeedType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class CheckAuctionHouseFeedJob @Autowired constructor(
        val httpRequestFactory: HttpRequestFactory,
        val auctionHouseFeedDao: AuctionHouseFeedDao,
        @Value("\${jobs.checkfeed.run}") val runJob: Boolean,
        @Value("\${hex.auction-house-feed.url}") val feedUrlBase: String
) {

    val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun run() {
        if (!runJob) {
            return;
        }
        log.info("Checking Auction House Feed")
        val url = feedUrlBase + "index.txt"
        val request = httpRequestFactory.buildGetRequest(GenericUrl(url))
        val response = request.execute()
        val stats = Stats()
        try {
            val feeds = auctionHouseFeedDao.findAll().map { it.filename }.toSet()
            val reader = response.content.bufferedReader()
            reader.forEachLine { line ->
                stats.lines++
                val filename = line.trim()
                when {
                    feeds.contains(filename) -> stats.exists++
                    line.contains("Data-Card") -> {
                        stats.newCardFeeds++
                        auctionHouseFeedDao.add(filename, AuctionHouseFeedType.Card)
                    }
                    line.contains("Data-Item") -> {
                        stats.newItemFeeds++
                        auctionHouseFeedDao.add(filename, AuctionHouseFeedType.Item)
                    }
                    else -> stats.ignored++
                }
            }
        } finally {
            response.disconnect()
            log.info("Checked Auction House Feed: $stats")
        }
    }

    private data class Stats(
            var lines: Int = 0,
            var ignored: Int = 0,
            var exists: Int = 0,
            var newCardFeeds: Int = 0,
            var newItemFeeds: Int = 0
    )

}
