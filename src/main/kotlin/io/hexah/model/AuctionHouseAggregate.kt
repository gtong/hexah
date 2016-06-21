package io.hexah.model

import java.util.*

data class AuctionHouseAggregate(
        val name: String,
        val rarity: HexObjectRarity,
        val nameKey: String,
        val currency: AuctionHouseCurrency,
        val created: Date,
        val updated: Date,
        val stats: Map<String, Number>
)
