package io.hexah.controller

import io.hexah.controller.handler.NotFoundException
import io.hexah.dao.UserDao
import io.hexah.model.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
open class SyncController @Autowired constructor(
        private val userDao: UserDao
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @RequestMapping(path = arrayOf("/api/sync/{guid}"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    open fun sync(@PathVariable guid: String) {
        var user: User
        try {
            user = userDao.findByGuid(guid)
        } catch (t: Throwable) {
            throw NotFoundException(t)
        }
        log.info("User: [{}]", user)
    }

}
