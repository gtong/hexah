package io.hexah.controller

import io.hexah.dao.AuctionHouseAggregateDao
import io.hexah.util.getFullName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
open class ObjectsController @Autowired constructor(val auctionHouseAggregateDao: AuctionHouseAggregateDao) {

    @RequestMapping(path = arrayOf("/api/objects/"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    open fun index(): List<ObjectResponse> {
        return auctionHouseAggregateDao
                .findNames()
                .map { ObjectResponse(it.third, getFullName(it.first, it.second)) }
                .sortedBy { it.name }
    }

}

data class ObjectResponse(val id: String, val name: String)
