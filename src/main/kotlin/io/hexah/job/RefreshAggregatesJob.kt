package io.hexah.job

import io.hexah.dao.AuctionHouseAggregateDao
import io.hexah.dao.AuctionHouseDataDao
import io.hexah.util.averageDouble
import io.hexah.util.averageInt
import io.hexah.util.getNameKey
import io.hexah.util.round
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
open class RefreshAggregatesJob @Autowired constructor(
        private val auctionHouseAggregateDao: AuctionHouseAggregateDao,
        private val auctionHouseDataDao: AuctionHouseDataDao
) {
    private val DAY_IN_MS = 1000 * 60 * 60 * 24

    private val log = LoggerFactory.getLogger(javaClass)

    fun run() {
        log.info("Refreshing auction house aggregates")
        val stats = Stats()
        auctionHouseDataDao.findAllNames().forEach { name ->
            calculateAggregate(name)
            stats.updated++
        }
        log.info("Refreshed auction house aggregates: $stats")
    }

    private fun calculateAggregate(name: String) {
        val now = Date()
        val data = auctionHouseDataDao.findByName(name).sortedBy { it.date }
        val last7Date = Date(now.time - 8 * DAY_IN_MS)
        val totalDays = (now.time - data.first().date.time) / DAY_IN_MS + 1
        data.groupBy { Pair(it.rarity, it.currency) }.forEach { entry ->
            val (rarity, currency) = entry.key
            val totalData = entry.value
            val last7Data = totalData.filter { it.date.time > last7Date.time };
            val recentData = if (totalData.size > 1) totalData.reversed().slice(0..0) else totalData
            auctionHouseAggregateDao.save(
                    name = name,
                    rarity = rarity,
                    nameKey = getNameKey(name, rarity),
                    currency = currency,
                    stats = mapOf(
                            "recent_trades" to recentData.sumBy { it.trades },
                            "recent_median" to recentData.sumBy { it.median },
                            "recent_average" to round(recentData.sumByDouble { it.average }),
                            "last_7_trades" to last7Data.sumBy { it.trades },
                            "last_7_trades_per_day" to round(last7Data.sumBy { it.trades }.toDouble() / 7),
                            "last_7_average_median" to round(averageInt(last7Data.map { it.median })),
                            "last_7_average_average" to round(averageDouble(last7Data.map { it.average })),
                            "total_trades" to totalData.sumBy { it.trades },
                            "total_trades_per_day" to round(totalData.sumBy { it.trades }.toDouble() / totalDays),
                            "total_average_median" to round(averageInt(totalData.map { it.median })),
                            "total_average_average" to round(averageDouble(totalData.map { it.average }))
                    )
            )
        }
    }

    private data class Stats(
            var updated: Int = 0
    )
}
