package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.tag.innsynAareg.client.altinn.AltinnClient
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
import no.nav.tag.innsynAareg.utils.FnrExtractor
import org.springframework.http.ResponseEntity

import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

@RestController
@Protected
class EnhetsregisteretController(val enhetsregisteretClient: EnhetsregisteretClient, val altinnClient: AltinnClient,
                                 val requestContextHolder: TokenValidationContextHolder) {
    @GetMapping(value = ["/tidligere-virksomheter"])
    fun hentTidligerVirksomheter(
            @RequestHeader("jurenhet") juridiskEnhetOrgnr: String,
            @ApiIgnore @CookieValue("selvbetjening-idtoken") idToken: String
    ): ResponseEntity<List<Organisasjon>> {
        val fnr: String = FnrExtractor.extract(requestContextHolder)
        val result = enhetsregisteretClient.finnTidligereVirksomheter(juridiskEnhetOrgnr )
        return ResponseEntity.ok(result!!)
    }

}
