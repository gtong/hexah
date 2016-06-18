package io.hexah.util

import io.hexah.model.HexObjectRarity

fun getName(name: String, rarity: HexObjectRarity): String = if (rarity == HexObjectRarity.Epic) name + " (AA)" else name


