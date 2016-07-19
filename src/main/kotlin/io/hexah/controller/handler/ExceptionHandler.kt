package io.hexah.controller.handler

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ControllerAdvice
open class ExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(Throwable::class)
    @ResponseBody
    open fun handleException(t: Throwable, request: HttpServletRequest, response: HttpServletResponse): ErrorResponse {
        log.error("Error processing request {}", request.requestURI, t)
        if (t is ControllerException) {
            response.status = t.status.value()
            return ErrorResponse(t.status.value(), t.reason)
        } else {
            response.status = HttpStatus.BAD_REQUEST.value()
            return ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Could not process request")
        }
    }
}

data class ErrorResponse(
        val status: Int,
        val message: String,
        val timestamp: Date = Date()
)

abstract class ControllerException : RuntimeException {
    val status: HttpStatus
    val reason: String

    constructor(status: HttpStatus, reason: String) : super() {
        this.status = status
        this.reason = reason
    }

    constructor(status: HttpStatus, reason: String, cause: Throwable) : super(cause) {
        this.status = status
        this.reason = reason
    }

}

class NotFoundException : ControllerException {
    constructor() : super(HttpStatus.BAD_REQUEST, "Not found")
    constructor(t: Throwable) : super(HttpStatus.BAD_REQUEST, "Not found", t)
}

class UnauthorizedException : ControllerException {
    constructor() : super(HttpStatus.UNAUTHORIZED, "Unauthorized")
}
