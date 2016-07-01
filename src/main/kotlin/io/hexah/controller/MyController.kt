package io.hexah.controller

import io.hexah.controller.handler.Authenticated
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseBody

@Controller
open class MyController {

    @RequestMapping(path = arrayOf("/api/my/profile"), method = arrayOf(RequestMethod.GET))
    @ResponseBody
    @Authenticated
    open fun profile(): ProfileResponse {
        return ProfileResponse("test")
    }

}

data class ProfileResponse(val name: String)



