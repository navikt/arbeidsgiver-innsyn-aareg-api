package no.nav.tag.innsynAareg.utils

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.stereotype.Component

const val ACR_CLAIM_OLD = "acr=Level4"
const val ACR_CLAIM_NEW = "acr=idporten-loa-high"
const val ISSUER = "tokendings"

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