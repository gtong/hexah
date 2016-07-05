package io.hexah.controller.handler

import com.auth0.authentication.AuthenticationAPIClient
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
    private val AUTH_USER_ID_KEY = "auth.user_id"
    private val AUTH_TIMESTAMP_KEY = "auth.timestamp"
    private val AUTH_TIMEOUT = 1000 * 60 * 10

    private var users : Map<String, Int> = emptyMap()

    override fun preHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?): Boolean {
        if (handler is HandlerMethod && handler.getMethodAnnotation(Authenticated::class.java) != null) {
            if (!loggedIn()) {
                response?.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                return false
            }
        }
        return super.preHandle(request, response, handler)
    }

    fun loggedIn(): Boolean {
        val timestamp = session()?.getAttribute(AUTH_TIMESTAMP_KEY)
        if (timestamp is Long) {
            if (Date().time - timestamp > AUTH_TIMEOUT) {
                return false
            } else if (session()?.getAttribute(AUTH_USER_ID_KEY) is Int) {
                return true
            }
        }
        return false
    }

    fun loginToken(token: String): Boolean {
        try {
            val profile = authClient.tokenInfo(token).execute()
            val userId = userManager.getOrCreateUserId(profile.email)
            session()?.setAttribute(AUTH_TIMESTAMP_KEY, Date().time)
            session()?.setAttribute(AUTH_USER_ID_KEY, userId)
            return true
        } catch (t: Throwable) {
            log.error("Could not authenticate token", t)
        }
        return false
    }

    fun userId(): Int? {
        return session()?.getAttribute(AUTH_USER_ID_KEY) as? Int
    }

    fun logout() {
        session()?.removeAttribute(AUTH_TIMESTAMP_KEY)
        session()?.removeAttribute(AUTH_USER_ID_KEY)
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
