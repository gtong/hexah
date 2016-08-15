package io.hexah.controller

import io.hexah.dao.AuctionHouseAggregateDao
import io.hexah.dao.UserItemAvailabilityDao
import io.hexah.model.HexObjectType
import io.hexah.util.getFullName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import java.util.*

const val DAY_IN_MS = 1000 * 60 * 60 * 24

@Controller
open class ObjectsController @Autowired constructor(
        private val auctionHouseAggregateDao: AuctionHouseAggregateDao,
        private val userItemAvailabilityDao: UserItemAvailabilityDao
) {

    @RequestMapping(path = arrayOf("/api/objects/"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    open fun index(): List<ObjectResponse> {
        return auctionHouseAggregateDao
                .findNames()
                .map { ObjectResponse(it.third, getFullName(it.first, it.second)) }
                .sortedBy { it.name }
    }

    @RequestMapping(path = arrayOf("/api/objects/{key}/available"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    open fun available(@PathVariable key: String): List<AvailabilityResponse> {
        return userItemAvailabilityDao
                .findAvailabileByNameKey(key)
                .sortedBy { -it.userLastActive.time }
                .map {
                    val diff = Date().time - it.userLastActive.time
                    AvailabilityResponse(
                            userId = it.userId,
                            lastActive =
                            if (diff < DAY_IN_MS) "Today"
                            else if (diff < DAY_IN_MS * 3) "Last 3 Days"
                            else if (diff < DAY_IN_MS * 7) "This Week"
                            else "More Than a Week",
                            available = if (it.hexObjectType == HexObjectType.Equipment) {
                                if (it.numberAvailable > 1) "> 1" else it.numberAvailable.toString()
                            } else {
                                if (it.numberAvailable > 4) "> 4" else it.numberAvailable.toString()
                            }
                    )
                }
    }

}

data class ObjectResponse(
        val id: String,
        val name: String
)

data class AvailabilityResponse(
        val userId: Int,
        val lastActive: String,
        val available: String
)
