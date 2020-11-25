package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.tag.innsynAareg.client.altinn.AltinnClient
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.models.AltinnIngenRettigheter
import no.nav.tag.innsynAareg.models.AltinnOppslagVellykket
import no.nav.tag.innsynAareg.utils.FnrExtractor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer="selvbetjening", claimMap= ["acr=Level4"])
@RestController
class OrganisasjonController(
    val altinnClient: AltinnClient,
    val requestContextHolder: TokenValidationContextHolder
) {

    @GetMapping(value = ["/organisasjoner"])
    fun hentOrganisasjoner(): ResponseEntity<List<Organisasjon>> {
        val fnr: String = FnrExtractor.extract(requestContextHolder)
        return when (val result = altinnClient.hentOrganisasjoner(fnr)) {
            is AltinnOppslagVellykket -> ResponseEntity.ok(result.organisasjoner)
            AltinnIngenRettigheter -> ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @GetMapping(value = ["/rettigheter-til-tjeneste"])
    fun hentRettigheter(
        @RequestParam serviceKode: String?,
        @RequestParam serviceEdition: String?
    ): ResponseEntity<List<Organisasjon>> {
        val fnr: String = FnrExtractor.extract(requestContextHolder)
        val result = altinnClient.hentOrganisasjonerBasertPaRettigheter(fnr, serviceKode!!, serviceEdition!!)
        return when (result) {
            is AltinnOppslagVellykket -> ResponseEntity.ok(result.organisasjoner)
            AltinnIngenRettigheter -> ResponseEntity(HttpStatus.FORBIDDEN)

        }
    }

}

