package io.hexah.model

import java.util.*

enum class AuctionHouseFeedType {
    c, i
}

class AuctionHouseFeed(
        val filename: String,
        val created: Date,
        val updated: Date,
        val type: AuctionHouseFeedType,
        var inProgress: Boolean,
        var numLoaded: Int,
        var completed: Date?
)
