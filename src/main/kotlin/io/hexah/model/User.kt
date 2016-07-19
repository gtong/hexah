package io.hexah.model

import java.util.*

enum class UserStatus(val db: Char) {
    Unverified('u'), Verified('v');

    companion object {
        fun fromDB(db: Char): UserStatus = UserStatus.values().single { it.db == db }
    }
}

data class User(
        val id: Int,
        val created: Date,
        val updated: Date,
        val email: String,
        val status: UserStatus,
        val guid: String,
        val selling: Boolean,
        val lastActive: Date,
        val hexUser: String?
)
