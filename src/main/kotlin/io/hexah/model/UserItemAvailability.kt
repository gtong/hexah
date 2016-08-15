package io.hexah.model

import java.util.*

data class UserItemAvailability(
        val userId: Int,
        val userLastActive: Date,
        val hexObjectType: HexObjectType,
        val numberAvailable: Int
)
