package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
import org.springframework.http.ResponseEntity

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tag.innsynAareg.utils.ISSUER
import no.nav.tag.innsynAareg.utils.LEVEL
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

@RestController
@ProtectedWithClaims(issuer = ISSUER, claimMap = [LEVEL])
class EnhetsregisteretController(
    val enhetsregisteretClient: EnhetsregisteretClient,
    val requestContextHolder: TokenValidationContextHolder
) {
    @GetMapping(value = ["/tidligere-virksomheter"])
    fun hentTidligerVirksomheter(
        @RequestHeader("jurenhet") juridiskEnhetOrgnr: String,
        @ApiIgnore @CookieValue("selvbetjening-idtoken") idToken: String
    ): ResponseEntity<List<Organisasjon>> {
        val result = enhetsregisteretClient.finnTidligereVirksomheter(juridiskEnhetOrgnr, idToken)
        return ResponseEntity.ok(result!!)
    }
}