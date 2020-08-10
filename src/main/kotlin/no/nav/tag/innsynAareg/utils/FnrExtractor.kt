package no.nav.tag.innsynAareg.utils

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder

object FnrExtractor {
    var ISSUER_SELVBETJENING = "selvbetjening"

    fun extract(ctxHolder: TokenValidationContextHolder): String {
        val context: TokenValidationContext = ctxHolder.tokenValidationContext
        return context.getClaims(ISSUER_SELVBETJENING).subject
    }
}
