package io.hexah.controller

import io.hexah.dao.AuctionHouseAggregateDao
import io.hexah.model.HexObjectRarity
import io.hexah.util.getName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
open class ObjectController @Autowired constructor(val auctionHouseAggregateDao: AuctionHouseAggregateDao) {

    @RequestMapping(path = arrayOf("/api/objects/"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    open fun index(): List<ObjectResponse> {
        return auctionHouseAggregateDao
                .findAllNameRarity()
                .map { ObjectResponse(getName(it.first, it.second)) }
                .sortedBy { it.name }
    }

}

data class ObjectResponse(val name: String)
