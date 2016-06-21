package io.hexah.util

import io.hexah.model.HexObjectRarity

fun getFullName(name: String, rarity: HexObjectRarity): String
        = if (rarity == HexObjectRarity.Epic) name + " (AA)" else name

fun getNameKey(name: String, rarity: HexObjectRarity): String
        = name.toLowerCase().replace(" ", "-").replace("""\W""", "") + if (rarity == HexObjectRarity.Epic) "-aa" else ""


