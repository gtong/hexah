package io.hexah.job

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import io.hexah.dao.AuctionHouseDataDao
import io.hexah.dao.AuctionHouseFeedDao
import io.hexah.dao.HexObjectDao
import io.hexah.model.AuctionHouseCurrency
import io.hexah.model.AuctionHouseFeed
import io.hexah.model.AuctionHouseFeedType
import io.hexah.model.HexObjectRarity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@Component
open class LoadAuctionHouseDataJob @Autowired constructor(
        val httpRequestFactory: HttpRequestFactory,
        val auctionHouseFeedDao: AuctionHouseFeedDao,
        val auctionHouseDataDao: AuctionHouseDataDao,
        val hexObjectDao: HexObjectDao,
        val txManager: PlatformTransactionManager,
        @Value("\${jobs.feed.run}") val runJob: Boolean,
        @Value("\${jobs.feed.threads}") val threads: Int,
        @Value("\${hex.auction-house-feed.url}") val feedUrlBase: String
) {
    val log = LoggerFactory.getLogger(javaClass)
    val txDefinition = DefaultTransactionDefinition()
    val executor = Executors.newFixedThreadPool(threads)
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd")

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun run() {
        val feed = auctionHouseFeedDao.findOneToProcessByType(AuctionHouseFeedType.Card)
        if (feed != null) {
            val stats = processCardFeed(feed)
            feed.numLoaded = stats.rowsSaved
            feed.inProgress = false
            feed.completed = Date()
            auctionHouseFeedDao.save(feed)
        }
    }

    private fun getNameLookup(): Map<String, String> {
        val nameLookup = HashMap<String, String>()
        nameLookup.putAll(
                hexObjectDao.findAll().map { it.name }.associate { Pair(it.replace(",", "").toLowerCase(), it) }
        )
        nameLookup.put("murder", "Kill")
        return nameLookup
    }

    private fun processCardFeed(feed : AuctionHouseFeed): CardStats {
        log.info("Loading Auction House Card Data from ${feed.filename}")
        val stats = CardStats()
        var nameLookup = getNameLookup()
        val url = feedUrlBase + feed.filename
        val request = httpRequestFactory.buildGetRequest(GenericUrl(url))
        val response = request.execute()
        try {
            var currentName = ""
            var rows = ArrayList<CardRow>()
            response.content.bufferedReader().forEachLine { line ->
                stats.lines++
                val row = CardRow(line, dateFormatter)
                if (!currentName.equals(row.name)) {
                    val r = ArrayList<CardRow>(rows)
                    executor.execute { processCardRows(stats, nameLookup, r) }
                    rows.clear()
                    currentName = row.name
                }
                rows.add(row)
            }
        } finally {
            response.disconnect()
            log.info("Loaded Auction House Card Data: $stats")
        }
        return stats
    }

    private fun processCardRows(stats: CardStats, nameLookup: Map<String, String>, rows : List<CardRow>) {
        if (rows.size == 0) {
            return
        }
        stats.cards++
        val ref = rows.first()
        val name = nameLookup[ref.name.toLowerCase()]
        if (name == null) {
            log.error("Could not find card [${ref.name}]")
            stats.nameErrors++
            return
        }
        val tx = txManager.getTransaction(txDefinition)
        try {
            var adds = 0;
            rows.groupBy { Pair(it.rarity, it.currency) }.forEach { entry ->
                val sorted = entry.value.map { it.price }.sorted()
                auctionHouseDataDao.add(
                        date = ref.date,
                        name = name,
                        rarity = entry.key.first,
                        currency = entry.key.second,
                        trades = sorted.size,
                        low = sorted.first(),
                        high = sorted.last(),
                        median = median(sorted),
                        average = average(sorted)
                )
                adds++
            }
            txManager.commit(tx)
            stats.rowsSaved += adds
        } catch (t: Throwable) {
            txManager.rollback(tx)
            stats.rollbacks++
            throw t
        }
    }

    private fun median(sorted: List<Int>): Int {
        if (sorted.size % 2 == 0) {
            val mid = sorted.size / 2 - 1
            return Math.round(sorted[mid] + sorted[mid + 1] / 2.0).toInt()
        } else {
            return sorted[sorted.size / 2]
        }
    }

    private fun average(values: List<Int>): Double {
        return values.sum().toDouble() / values.size
    }

    private class CardRow {
        val name: String
        val rarity: HexObjectRarity
        val currency: AuctionHouseCurrency
        val price: Int
        val date: Date

        constructor(line: String, dateFormatter: SimpleDateFormat) {
            val l = line.split(",").map { it.trim() }
            this.name = l[0]
            this.rarity = HexObjectRarity.fromDB(l[1].toInt())
            this.currency = AuctionHouseCurrency.fromFeed(l[2])
            this.price = l[3].toInt()
            this.date = dateFormatter.parse(l[4])
        }
    }

    private data class CardStats(
            var lines: Int = 0,
            var cards: Int = 0,
            var nameErrors: Int = 0,
            var rowsSaved: Int = 0,
            var rollbacks: Int = 0
    )
}
