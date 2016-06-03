package io.hexah.job

import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpRequestFactory
import io.hexah.dao.AuctionHouseAggregateDao
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
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@Component
open class LoadAuctionHouseDataJob @Autowired constructor(
        val auctionHouseAggregateDao: AuctionHouseAggregateDao,
        val auctionHouseDataDao: AuctionHouseDataDao,
        val auctionHouseFeedDao: AuctionHouseFeedDao,
        val hexObjectDao: HexObjectDao,
        val httpRequestFactory: HttpRequestFactory,
        val txManager: PlatformTransactionManager,
        @Value("\${jobs.feed.run}") val runJob: Boolean,
        @Value("\${jobs.feed.threads}") val threads: Int,
        @Value("\${hex.auction-house-feed.url}") val feedUrlBase: String
) {
    val DAY_IN_MS = 1000 * 60 * 60 * 24

    val log = LoggerFactory.getLogger(javaClass)
    val txDefinition = DefaultTransactionDefinition()
    val executor = Executors.newFixedThreadPool(threads)
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd")

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    fun run() {
        val tx = txManager.getTransaction(txDefinition)
        var feed: AuctionHouseFeed? = null
        try {
            feed = auctionHouseFeedDao.findOneToProcessByType(AuctionHouseFeedType.Card)
            if (feed != null) {
                feed.inProgress = true
                auctionHouseFeedDao.update(feed)
            }
            txManager.commit(tx)
        } catch (t: Throwable) {
            txManager.rollback(tx)
        }
        if (feed != null) {
            val stats = processCardFeed(feed)
            feed.numLoaded = stats.lines
            feed.inProgress = false
            feed.completed = Date()
            auctionHouseFeedDao.update(feed)
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

    private fun processCardFeed(feed: AuctionHouseFeed): CardStats {
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

    private fun processCardRows(stats: CardStats, nameLookup: Map<String, String>, rows: List<CardRow>) {
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
            calculateAggregate(name)
            txManager.commit(tx)
            stats.rowsSaved += adds
        } catch (t: Throwable) {
            txManager.rollback(tx)
            stats.rollbacks++
            throw t
        }
    }

    private fun calculateAggregate(name: String) {
        val now = Date()
        val data = auctionHouseDataDao.findByName(name)
        val last7date = Date(now.time - 8 * DAY_IN_MS)
        val daysInTrading = (now.time - data.first().date.time) / DAY_IN_MS + 1
        data.groupBy { Pair(it.rarity, it.currency) }.forEach { entry ->
            val totalData = entry.value
            val last7data = totalData.filter { it.date.time >= last7date.time }
            auctionHouseAggregateDao.save(
                    name = name,
                    rarity = entry.key.first,
                    currency = entry.key.second,
                    stats = mapOf(
                            "total_trades" to totalData.sumBy { it.trades },
                            "total_trades_per_day" to round(totalData.sumBy { it.trades }.toDouble() / daysInTrading),
                            "total_average_median" to round(average(totalData.map { it.median })),
                            "total_average_average" to round(averageDouble(totalData.map { it.average })),
                            "last_7_trades" to last7data.sumBy { it.trades },
                            "last_7_trades_per_day" to round(last7data.sumBy { it.trades }.toDouble() / daysInTrading),
                            "last_7_average_median" to round(average(last7data.map { it.median })),
                            "last_7_average_average" to round(averageDouble(last7data.map { it.average }))
                    )
            )
        }
    }

    private fun round(value: Double): BigDecimal {
        val format = DecimalFormat("#.0000")
        format.roundingMode = RoundingMode.HALF_UP
        return BigDecimal(format.format(value))
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
        if (values.size == 0) {
            return 0.0
        }
        return values.sum().toDouble() / values.size
    }

    private fun averageDouble(values: List<Double>): Double {
        if (values.size == 0) {
            return 0.0
        }
        return values.sum() / values.size
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
