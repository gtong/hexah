package io.hexah.controller.handler

import com.auth0.authentication.AuthenticationAPIClient
import io.hexah.controller.handler.UnauthorizedException
import io.hexah.dao.UserDao
import io.hexah.manager.UserManager
import io.hexah.model.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@Component
open class AuthHandler @Autowired constructor(
    private val authClient : AuthenticationAPIClient,
    private val userManager: UserManager
): HandlerInterceptorAdapter() {

    private val log = LoggerFactory.getLogger(javaClass)
    private val AUTH_HEADER_TOKEN = "token"
    private val AUTH_SESSION_USER_ID = "auth.user_id"
    private val AUTH_SESSION_TIMESTAMP = "auth.timestamp"
    private val AUTH_TIMEOUT = 1000 * 60 * 10

    private var users : Map<String, Int> = emptyMap()

    override fun preHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?): Boolean {
        if (handler is HandlerMethod && handler.getMethodAnnotation(Authenticated::class.java) != null) {
            if (!loggedIn()) {
                val token: String? = request?.getHeader(AUTH_HEADER_TOKEN)
                if (token is String && loginToken(token)) {
                    return super.preHandle(request, response, handler)
                } else {
                    response?.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                    return false
                }
            }
        }
        return super.preHandle(request, response, handler)
    }

    fun loggedIn(): Boolean {
        val timestamp = session()?.getAttribute(AUTH_SESSION_TIMESTAMP)
        if (timestamp is Long) {
            if (Date().time - timestamp > AUTH_TIMEOUT) {
                return false
            } else if (session()?.getAttribute(AUTH_SESSION_USER_ID) is Int) {
                return true
            }
        }
        return false
    }

    fun loginToken(token: String): Boolean {
        try {
            val profile = authClient.tokenInfo(token).execute()
            val userId = userManager.getOrCreateUserId(profile.email)
            session()?.setAttribute(AUTH_SESSION_TIMESTAMP, Date().time)
            session()?.setAttribute(AUTH_SESSION_USER_ID, userId)
            return true
        } catch (t: Throwable) {
            log.error("Could not authenticate token", t)
        }
        return false
    }

    fun userId(): Int {
        val userId : Any? = session()?.getAttribute(AUTH_SESSION_USER_ID)
        if (userId is Int) {
            return userId
        } else {
            throw UnauthorizedException()
        }
    }

    fun logout() {
        session()?.removeAttribute(AUTH_SESSION_TIMESTAMP)
        session()?.removeAttribute(AUTH_SESSION_USER_ID)
    }

    private fun session(): HttpSession? {
        val attributes = RequestContextHolder.currentRequestAttributes()
        if (attributes is ServletRequestAttributes) {
            return attributes.request.session
        } else {
            return null
        }
    }

}

@Target(AnnotationTarget.FUNCTION)
annotation class Authenticated
