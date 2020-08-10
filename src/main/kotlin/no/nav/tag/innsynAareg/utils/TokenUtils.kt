package no.nav.tag.innsynAareg.utils

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.tag.innsynAareg.utils.FnrExtractor.ISSUER_SELVBETJENING
import org.springframework.stereotype.Component

@Component
class TokenUtils(requestContextHolder: TokenValidationContextHolder) {
    private val requestContextHolder: TokenValidationContextHolder = requestContextHolder

    val tokenForInnloggetBruker: String
        get() = requestContextHolder.tokenValidationContext.getJwtToken(ISSUER_SELVBETJENING).tokenAsString

}