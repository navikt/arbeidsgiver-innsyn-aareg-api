package no.nav.tag.innsynAareg.utils

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder

import no.nav.security.token.support.spring.SpringTokenValidationContextHolder


object FnrExtractor {
    var ISSUER_SELVBETJENING = "selvbetjening"
    fun extract(ctxHolder: TokenValidationContextHolder): String {
        val context: TokenValidationContext = ctxHolder.tokenValidationContext.anyValidClaims.
                .

                getRequestAttribute(OIDCConstants.OIDC_VALIDATION_CONTEXT) as OIDCValidationContext
        return context.getClaims(ISSUER_SELVBETJENING).getClaimSet().getSubject()
    }
}