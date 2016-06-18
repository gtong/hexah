package io.hexah.model

import java.util.*

enum class AuctionHouseCurrency(val db: Char) {
    Gold('g'), Platinum('p');

    companion object {
        fun fromDB(db: Char): AuctionHouseCurrency {
            return AuctionHouseCurrency.values().single { it.db == db }
        }
        fun fromFeed(value: String): AuctionHouseCurrency {
            return AuctionHouseCurrency.values().single { it.name.equals(value, true) }
        }
    }
}

data class AuctionHouseData(
        val date: Date,
        val name: String,
        val rarity: HexObjectRarity,
        val currency: AuctionHouseCurrency,
        val created: Date,
        val updated: Date,
        val trades: Int,
        val low: Int,
        val high: Int,
        val median: Int,
        val average: Double
)
