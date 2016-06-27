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
import io.hexah.util.averageInt
import io.hexah.util.getNameKey
import io.hexah.util.median
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
import java.util.concurrent.Phaser
import java.util.concurrent.atomic.AtomicInteger

@Component
open class LoadAuctionHouseDataJob @Autowired constructor(
        val auctionHouseDataDao: AuctionHouseDataDao,
        val auctionHouseFeedDao: AuctionHouseFeedDao,
        val hexObjectDao: HexObjectDao,
        val httpRequestFactory: HttpRequestFactory,
        val txManager: PlatformTransactionManager,
        val refreshAggregatesJob: RefreshAggregatesJob,
        @Value("\${jobs.loadfeed.run}") val runJob: Boolean,
        @Value("\${jobs.loadfeed.threads}") val threads: Int,
        @Value("\${hex.auction-house-feed.url}") val feedUrlBase: String
) {
    val DAY_IN_MS = 1000 * 60 * 60 * 24

    val log = LoggerFactory.getLogger(javaClass)
    val txDefinition = DefaultTransactionDefinition()
    val executor = Executors.newFixedThreadPool(threads)

    @Scheduled(fixedDelayString = "\${jobs.loadfeed.delay}")
    fun run() {
        if (!runJob) {
            return;
        }
        val tx = txManager.getTransaction(txDefinition)
        var feed: AuctionHouseFeed? = null
        try {
            feed = auctionHouseFeedDao.findOneToProcessByType(AuctionHouseFeedType.All)
            if (feed != null) {
                feed.inProgress = true
                auctionHouseFeedDao.update(feed)
            }
            txManager.commit(tx)
        } catch (t: Throwable) {
            txManager.rollback(tx)
        }
        if (feed != null) {
            val stats = processFeed(feed)
            feed.numLoaded = stats.lines.get()
            feed.inProgress = false
            feed.completed = Date()
            auctionHouseFeedDao.update(feed)
        }
        refreshAggregatesJob.run()
    }

    private fun getNameLookup(): Map<String, String> {
        val nameLookup = HashMap<String, String>()
        nameLookup.putAll(
                hexObjectDao.findAll().map { it.name }.associate { Pair(it.replace(",", "").toLowerCase(), it) }
        )
        nameLookup.put("murder", "Kill")
        return nameLookup
    }

    private fun processFeed(feed: AuctionHouseFeed): Stats {
        log.info("Loading auction house data from ${feed.filename}")
        val stats = Stats()
        val url = feedUrlBase + feed.filename
        val request = httpRequestFactory.buildGetRequest(GenericUrl(url))
        val response = request.execute()

        var nameLookup = getNameLookup()
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd")
        val phaser = Phaser()

        try {
            phaser.register()
            var currentName = ""
            var lines = ArrayList<FeedLine>()
            response.content.bufferedReader().forEachLine { text ->
                stats.lines.incrementAndGet()
                try {
                    val line = FeedLine(text, dateFormatter)
                    if (!currentName.equals(line.name)) {
                        val l = ArrayList<FeedLine>(lines)
                        phaser.register()
                        executor.execute {
                            try {
                                processLines(stats, nameLookup, l)
                            } finally {
                                phaser.arrive()
                            }
                        }
                        lines.clear()
                        currentName = line.name
                    }
                    lines.add(line)
                } catch (t: Throwable) {
                    log.error("Error processing [$text]", t);
                }
            }
            phaser.arriveAndAwaitAdvance()
        } finally {
            response.disconnect()
            log.info("Loaded auction house data from ${feed.filename}: $stats")
        }
        return stats
    }

    private fun processLines(stats: Stats, nameLookup: Map<String, String>, lines: List<FeedLine>) {
        if (lines.size == 0) {
            return
        }
        stats.lines.incrementAndGet()
        val ref = lines.first()
        val name = nameLookup[ref.name.toLowerCase()]
        if (name == null) {
            log.error("Could not find item [${ref.name}]")
            stats.nameErrors.incrementAndGet()
            return
        }
        val tx = txManager.getTransaction(txDefinition)
        try {
            var adds = 0;
            lines.groupBy { Pair(it.rarity, it.currency) }.forEach { entry ->
                val (rarity, currency) = entry.key
                val sorted = entry.value.map { it.price }.sorted()
                auctionHouseDataDao.add(
                        date = ref.date,
                        name = name,
                        rarity = rarity,
                        nameKey = getNameKey(name, rarity),
                        currency = currency,
                        trades = sorted.size,
                        low = sorted.first(),
                        high = sorted.last(),
                        median = median(sorted),
                        average = averageInt(sorted)
                )
                adds++
            }
            txManager.commit(tx)
            stats.itemsSaved.addAndGet(adds)
        } catch (t: Throwable) {
            txManager.rollback(tx)
            stats.rollbacks.incrementAndGet()
            throw t
        }
    }

    private class FeedLine {
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

    private data class Stats(
            val lines: AtomicInteger = AtomicInteger(0),
            val items: AtomicInteger = AtomicInteger(0),
            val nameErrors: AtomicInteger = AtomicInteger(0),
            val itemsSaved: AtomicInteger = AtomicInteger(0),
            val rollbacks: AtomicInteger = AtomicInteger(0)
    )
}
