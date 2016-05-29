package io.hexah.model

import java.util.*

enum class AuctionHouseFeedType(val db: Char) {
    Card('c'), Item('i');

    companion object {
        fun fromDB(db: Char): AuctionHouseFeedType {
            return AuctionHouseFeedType.values().single { it.db == db }
        }
    }
}

data class AuctionHouseFeed(
        val filename: String,
        val created: Date,
        val updated: Date,
        val type: AuctionHouseFeedType,
        var inProgress: Boolean,
        var numLoaded: Int,
        var completed: Date?
)
