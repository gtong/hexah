package io.hexah.controller

import io.hexah.dao.AuctionHouseAggregateDao
import io.hexah.model.AuctionHouseCurrency
import io.hexah.util.getFullName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
open class FeedsController @Autowired constructor(val auctionHouseAggregateDao: AuctionHouseAggregateDao) {

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
                item.nameKey,
                getFullName(item.name, item.rarity),
                if (gold == null) emptyMap() else gold.stats,
                if (plat == null) emptyMap() else plat.stats
        )
    }

    data class FeedSummaryResponse(
            val key: String,
            val name: String,
            val gold: Map<String, Number>,
            val platinum: Map<String, Number>
    )

}
