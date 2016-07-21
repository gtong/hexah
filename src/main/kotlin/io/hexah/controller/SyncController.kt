package io.hexah.controller

import com.google.gson.JsonParser
import io.hexah.controller.handler.NotFoundException
import io.hexah.dao.UserDao
import io.hexah.manager.UserItemManager
import io.hexah.model.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest

@Controller
open class SyncController @Autowired constructor(
        private val userDao: UserDao,
        private val userItemManager: UserItemManager
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val parser = JsonParser()

    @RequestMapping(path = arrayOf("/api/sync/{guid}"), method = arrayOf(RequestMethod.POST))
    @ResponseBody
    open fun sync(@PathVariable guid: String, request: HttpServletRequest) {
        var user: User
        try {
            user = userDao.findByGuid(guid)
        } catch (t: Throwable) {
            throw NotFoundException(t)
        }
        val json = parser.parse(request.reader).asJsonObject
        log.info("Sync: ${json["Message"]}")
        userItemManager.sync(user, json)
    }

}
