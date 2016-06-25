package io.hexah.util

import io.hexah.model.HexObjectRarity

val regex = """[^ \w]""".toRegex()

fun getFullName(name: String, rarity: HexObjectRarity): String
        = if (rarity == HexObjectRarity.Epic) name + " (AA)" else name

fun getNameKey(name: String, rarity: HexObjectRarity): String
        = name.toLowerCase().replace(regex, "").replace(' ', '-') + if (rarity == HexObjectRarity.Epic) "-aa" else ""


