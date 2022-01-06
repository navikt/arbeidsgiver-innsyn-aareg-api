package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.HttpClientErrorException.Forbidden
import org.springframework.web.client.HttpClientErrorException.Unauthorized
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestResponseEntityExceptionHandler : ResponseEntityExceptionHandler() {
    val log = LoggerFactory.getLogger(this::class.java)!!

    @ExceptionHandler(RuntimeException::class)
    @ResponseBody
    fun handleInternalError(e: RuntimeException, ignored: WebRequest?): ResponseEntity<Any> {
        log.error("Uhåndtert feil", e)
        return getResponseEntity(e, "Internal error", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(Forbidden::class)
    @ResponseBody
    protected fun handleForbidden(e: RuntimeException, ignored: WebRequest?): ResponseEntity<Any> {
        return getResponseEntity(e, "ingen tilgang", HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(JwtTokenUnauthorizedException::class, Unauthorized::class)
    @ResponseBody
    protected fun handleUnauthorized(e: RuntimeException, ignored: WebRequest?): ResponseEntity<Any> {
        return getResponseEntity(e, "ingen tilgang", HttpStatus.UNAUTHORIZED)
    }

    private fun getResponseEntity(
        t: Throwable,
        melding: String,
        status: HttpStatus,
    ): ResponseEntity<Any> {
        val body = FeilRespons(melding, t.message)
        log.info(String.format(
            "Returnerer følgende HttpStatus '%s' med melding '%s' pga exception '%s'",
            status.toString(),
            melding,
            t.message
        ), t
        )
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)
    }

    data class FeilRespons(
        val message: String? = null,
        val cause: String? = null,
    )
}