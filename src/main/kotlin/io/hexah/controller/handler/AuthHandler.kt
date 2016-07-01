package io.hexah.controller.handler

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@Component
open class AuthHandler: HandlerInterceptorAdapter() {
    private val log = LoggerFactory.getLogger(javaClass)
    private val USER_ID_KEY = "auth.user_id"

    override fun preHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?): Boolean {
        if (handler is HandlerMethod && handler.getMethodAnnotation(Authenticated::class.java) != null) {
            if (request?.session?.getAttribute(USER_ID_KEY) == null) {
                response?.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                return false
            }
        }
        return super.preHandle(request, response, handler)
    }

    fun userId(): Int? {
        return session()?.getAttribute(USER_ID_KEY) as? Int
    }

    fun logout() {
        session()?.removeAttribute(USER_ID_KEY)
    }

    private fun session(): HttpSession? {
        val attributes = RequestContextHolder.currentRequestAttributes()
        if (attributes is ServletRequestAttributes) {
            return attributes.request.session
        } else {
            return null;
        }
    }

}

@Target(AnnotationTarget.FUNCTION)
annotation class Authenticated
