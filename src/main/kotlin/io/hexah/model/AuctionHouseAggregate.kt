package io.hexah.model

import java.util.*

data class AuctionHouseAggregate(
        val name: String,
        val rarity: HexObjectRarity,
        val currency: AuctionHouseCurrency,
        val created: Date,
        val updated: Date,
        val totalTrades: Int,
        val totalTradesPerDay: Double,
        val totalMedian: Double,
        val totalAverage: Double,
        val last7Trades: Int,
        val last7TradesPerDay: Double,
        val last7Median: Double,
        val last7Average: Double
)
