package io.hexah.controller

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class NotFoundException: Throwable()

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
class UnauthorizedException: Throwable()
