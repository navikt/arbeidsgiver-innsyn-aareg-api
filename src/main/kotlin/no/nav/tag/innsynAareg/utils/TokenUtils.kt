package no.nav.tag.innsynAareg.utils

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.tag.innsynAareg.utils.FnrExtractor.ISSUER_SELVBETJENING
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

@Component
class TokenUtils(requestContextHolder: TokenValidationContextHolder) {
    private val requestContextHolder: TokenValidationContextHolder = requestContextHolder
    val ISSUER = "selvbetjening"
    val tokenForInnloggetBruker: String
        get() = requestContextHolder.tokenValidationContext.getJwtToken(ISSUER_SELVBETJENING).tokenAsString

    private fun claimSet(): JwtTokenClaims? {
        return Optional.ofNullable(context())
                .map(Function { s: TokenValidationContext -> s.getClaims(ISSUER) })
                .orElse(null)
    }
    private fun context(): TokenValidationContext? {
        return Optional.ofNullable(requestContextHolder.tokenValidationContext)
                .orElse(null)
    }
    fun getSubject(): String {
        return Optional.ofNullable(claimSet())
                .map(Function { obj: JwtTokenClaims -> obj.subject })
                .orElse(null)
    }
    private fun unauthenticated(msg: String): Supplier<out JwtTokenValidatorException>? {
        return Supplier { JwtTokenValidatorException(msg) }
    }
    fun autentisertBruker(): String? {
        return Optional.ofNullable<String>(getSubject())
                .orElseThrow(unauthenticated("Fant ikke subject"))
    }
}