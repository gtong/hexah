package io.hexah.controller

import io.hexah.controller.handler.NotFoundException
import io.hexah.dao.AuctionHouseAggregateDao
import io.hexah.dao.AuctionHouseDataDao
import io.hexah.model.AuctionHouseCurrency
import io.hexah.model.AuctionHouseData
import io.hexah.util.getFullName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
open class FeedsController @Autowired constructor(
        private val auctionHouseDataDao: AuctionHouseDataDao,
        private val auctionHouseAggregateDao: AuctionHouseAggregateDao
) {

    @RequestMapping(path = arrayOf("/api/feeds/{key}/"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    open fun index(@PathVariable key: String): FeedResponse {
        val data = auctionHouseDataDao.findByNameKey(key)
        if (data.size == 0) {
            throw NotFoundException()
        }
        return FeedResponse(
                key = key,
                gold = data.filter { it.currency == AuctionHouseCurrency.Gold }.map(::toFeedResponseItem),
                platinum = data.filter { it.currency == AuctionHouseCurrency.Platinum }.map(::toFeedResponseItem)
        )
    }

    @RequestMapping(path = arrayOf("/api/feeds/{key}/summary"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    open fun summary(@PathVariable key: String): FeedSummaryResponse {
        val aggregates = auctionHouseAggregateDao.findByNameKey(key)
        if (aggregates.size == 0) {
            throw NotFoundException()
        }
        val item = aggregates.first()
        val gold = aggregates.find { it.currency == AuctionHouseCurrency.Gold }
        val plat = aggregates.find { it.currency == AuctionHouseCurrency.Platinum }
        return FeedSummaryResponse(
                key = item.nameKey,
                name = getFullName(item.name, item.rarity),
                gold = if (gold == null) emptyMap() else gold.stats,
                platinum = if (plat == null) emptyMap() else plat.stats
        )
    }

}

data class FeedSummaryResponse(
        val key: String,
        val name: String,
        val gold: Map<String, Number>,
        val platinum: Map<String, Number>
)

data class FeedResponse(
        val key: String,
        val gold: List<FeedResponseItem>,
        val platinum: List<FeedResponseItem>
)

data class FeedResponseItem(
        val d: Long,
        val h: Int,
        val l: Int,
        val m: Int,
        val v: Int
)

fun toFeedResponseItem(value: AuctionHouseData) =
    FeedResponseItem(
            d = value.date.time,
            h = value.high,
            l = value.low,
            m = value.median,
            v = value.trades
    )

