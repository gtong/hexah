package io.hexah.controller

import io.hexah.controller.handler.AuthHandler
import io.hexah.controller.handler.Authenticated
import io.hexah.dao.UserDao
import io.hexah.model.UserStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
open class MyController @Autowired constructor(
        private val authHandler: AuthHandler,
        private val userDao: UserDao
) {

    @RequestMapping(path = arrayOf("/api/my/profile"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Authenticated
    open fun profile(): ProfileResponse {
        val user = userDao.findById(authHandler.userId())
        return ProfileResponse(user.email, user.guid, user.status, user.hexUser, user.sellCards, user.sellEquipment)
    }

}

data class ProfileResponse(
        val email: String,
        val guid: String,
        val status: UserStatus,
        val hexUser: String?,
        val sellCards: Boolean,
        val sellEquipment: Boolean
)



