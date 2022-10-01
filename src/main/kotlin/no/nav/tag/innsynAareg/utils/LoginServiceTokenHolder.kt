package no.nav.tag.innsynAareg.utils

import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpServletRequest

/**
 * TODO: Dette må vi se på før merge:
 * denne eksponerer loginservice token til bruk lenger nede i stacken, e.g. kall mot aareg-services
 * selvbetjening tokenet er ikke validert da vi nå gjør tokenx mellom frackend og backend.
 */
@Component
class LoginServiceTokenHolder {

    val idToken: String?
        get() = currentRequestCookies["selvbetjening-idtoken"]

    private val currentRequestCookies: Map<String, String>
        get() = currentRequest?.cookies?.associate { cookie -> cookie.name to cookie.value } ?: mapOf()

    private val currentRequest: HttpServletRequest?
        get() = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
}