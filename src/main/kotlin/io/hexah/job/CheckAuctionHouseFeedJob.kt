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
        private val httpRequestFactory: HttpRequestFactory,
        private val auctionHouseFeedDao: AuctionHouseFeedDao,
        @Value("\${jobs.checkfeed.run}") private val runJob: Boolean,
        @Value("\${hex.auction-house-feed.url}") private val feedUrlBase: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun run() {
        if (!runJob) {
            return;
        }
        log.info("Checking auction house feed")
        val url = feedUrlBase + "index.txt"
        val request = httpRequestFactory.buildGetRequest(GenericUrl(url))
        val response = request.execute()
        val stats = Stats()
        try {
            val feeds = auctionHouseFeedDao.findAll().map { it.filename }.toSet()
            val reader = response.content.bufferedReader()

            val regex = """AH-Data-\d{4}-\d{2}-\d{2}.csv""".toRegex()
            reader.forEachLine { line ->
                stats.lines++
                val filename = line.trim()
                when {
                    feeds.contains(filename) -> stats.exists++
                    regex.matches(filename) -> {
                        stats.newFeeds++
                        auctionHouseFeedDao.add(filename, AuctionHouseFeedType.All)
                    }
                    else -> stats.ignored++
                }
            }
        } finally {
            response.disconnect()
            log.info("Checked auction house feed: $stats")
        }
    }

    private data class Stats(
            var lines: Int = 0,
            var ignored: Int = 0,
            var exists: Int = 0,
            var newFeeds: Int = 0
    )

}
