package io.hexah.model

import java.util.*

enum class HexObjectType(val db: Char) {
    Card('c'), Equipment('e'), Mercenary('m'), Pack('p'), Stardust('s'), TreasureChest('t');

    companion object {
        fun fromDB(db: Char): HexObjectType {
            return HexObjectType.values().single { it.db == db }
        }
    }
}

enum class HexObjectRarity(val db: Int) {
    Promo(-2), Land(-1), Unknown(0), Common(2), Uncommon(3), Rare(4), Epic(5), Legendary(6);

    companion object {
        fun fromDB(db: Int): HexObjectRarity {
            return HexObjectRarity.values().single { it.db == db }
        }
    }
}

data class HexObject(
        val id: Int,
        val created: Date,
        val updated: Date,
        val guid: String,
        val setGuid: String?,
        val type: HexObjectType,
        val name: String,
        val rarity: HexObjectRarity,
        val nameKey: String,
        val alternateArt: Boolean
)
