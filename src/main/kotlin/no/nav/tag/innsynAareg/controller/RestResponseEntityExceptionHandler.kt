package no.nav.tag.innsynAareg.controller

import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode.Companion.BadGateway
import io.ktor.http.HttpStatusCode.Companion.GatewayTimeout
import io.ktor.http.HttpStatusCode.Companion.ServiceUnavailable
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.exceptions.AltinnrettigheterProxyKlientFallbackException
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

    @ExceptionHandler(AltinnrettigheterProxyKlientFallbackException::class)
    @ResponseBody
    protected fun handleAltinnFallbackFeil(
        e: AltinnrettigheterProxyKlientFallbackException,
        ignored: WebRequest?,
    ): ResponseEntity<Any> {
        if (e.cause is SocketTimeoutException) {
            return getResponseEntity(e, "Fallback til Altinn feilet pga timeout", HttpStatus.GATEWAY_TIMEOUT)
        }
        val httpStatus = hentDriftsforstyrrelse(e)
        return if (httpStatus != null) {
            getResponseEntity(e, "Fallback til Altinn feilet pga driftsforstyrrelse", httpStatus)
        } else {
            handleInternalError(e, ignored)
        }
    }

    private fun hentDriftsforstyrrelse(e: AltinnrettigheterProxyKlientFallbackException): HttpStatus? {
        return when (val c = e.cause) {
            is ServerResponseException -> when (c.response.status) {
                BadGateway,
                ServiceUnavailable,
                GatewayTimeout,
                -> HttpStatus.valueOf(c.response.status.value)

                else -> null
            }
            else -> null
        }
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