package io.hexah.model

import java.util.*

enum class UserItemType(val db: Char) {
    Card('c'), Inventory('i');

    companion object {
        fun fromDB(db: Char): UserItemType = UserItemType.values().single { it.db == db }
    }
}

data class UserItem(
        val userId: Int,
        val itemGuid: String,
        val type: UserItemType,
        val updated: Date,
        val number: Int,
        val inTransactions: Int,
        val sell: Boolean
)
