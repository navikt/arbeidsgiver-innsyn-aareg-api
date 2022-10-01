package no.nav.tag.innsynAareg.utils

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Component

const val LEVEL = "acr=Level4"
const val ISSUER = "tokenx"

@Component
class AutentisertBruker(
    val tokenValidationContextHolder: TokenValidationContextHolder
) {
    val fødselsnummer: String
        get() =
            tokenValidationContextHolder
                .tokenValidationContext
                .getClaims(ISSUER)
                .getStringClaim("pid")

    val jwtToken: String
        get() =
            tokenValidationContextHolder
                .tokenValidationContext
                .getJwtToken(ISSUER)
                .tokenAsString
}