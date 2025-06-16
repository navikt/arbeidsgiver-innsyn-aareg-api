package no.nav.tag.innsynAareg.utils

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.stereotype.Component

const val ACR_CLAIM_OLD = "acr=Level4"
const val ACR_CLAIM_NEW = "acr=idporten-loa-high"
const val ISSUER = "tokenx"

@Component
class AutentisertBruker(
    val tokenValidationContextHolder: TokenValidationContextHolder
) {
    private val jwtToken: JwtToken
        get() = tokenValidationContextHolder.getTokenValidationContext()
            .getJwtToken(ISSUER) ?: throw NoSuchElementException("no valid token. how did you get so far without a valid token?")


    val token: String
        get() = jwtToken.encodedToken

    val fødselsnummer: String
        get() = jwtToken.jwtTokenClaims.getStringClaim("pid")!!
}
