package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tag.innsynAareg.client.altinn.AltinnClient
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.models.AltinnIngenRettigheter
import no.nav.tag.innsynAareg.models.AltinnOppslagVellykket
import no.nav.tag.innsynAareg.utils.AutentisertBruker
import no.nav.tag.innsynAareg.utils.ISSUER
import no.nav.tag.innsynAareg.utils.LEVEL
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@ProtectedWithClaims(issuer = ISSUER, claimMap = [LEVEL])
@RestController
class OrganisasjonController(
    val altinnClient: AltinnClient,
    val autentisertBruker: AutentisertBruker
) {

    @GetMapping(value = ["/organisasjoner"])
    fun hentOrganisasjoner(): ResponseEntity<List<Organisasjon>> {
        val result = altinnClient.hentOrganisasjoner(
            autentisertBruker.fødselsnummer
        )
        return when (result) {
            is AltinnOppslagVellykket -> ResponseEntity.ok(result.organisasjoner)
            AltinnIngenRettigheter -> ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }

    @GetMapping(value = ["/rettigheter-til-tjeneste"])
    fun hentRettigheter(
        @RequestParam serviceKode: String?,
        @RequestParam serviceEdition: String?
    ): ResponseEntity<List<Organisasjon>> {
        val result = altinnClient.hentOrganisasjonerBasertPaRettigheter(
            autentisertBruker.fødselsnummer,
            serviceKode!!,
            serviceEdition!!
        )
        return when (result) {
            is AltinnOppslagVellykket -> ResponseEntity.ok(result.organisasjoner)
            AltinnIngenRettigheter -> ResponseEntity(HttpStatus.FORBIDDEN)
        }
    }
}

