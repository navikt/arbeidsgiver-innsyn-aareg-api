package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.tag.innsynAareg.models.altinn.Organisasjon
import no.nav.tag.innsynAareg.service.altinn.AltinnService
import no.nav.tag.innsynAareg.utils.FnrExtractor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Protected
@RestController
class OrganisasjonController(
    val altinnService: AltinnService,
    val requestContextHolder: TokenValidationContextHolder
) {

    @GetMapping(value = ["/organisasjoner"])
    fun hentOrganisasjoner(): ResponseEntity<List<Organisasjon?>> {
        val fnr: String = FnrExtractor.extract(requestContextHolder)
        val result = altinnService.hentOrganisasjoner(fnr)
        return ResponseEntity.ok(result!!)
    }

    @GetMapping(value = ["/rettigheter-til-tjeneste"])
    fun hentRettigheter(
        @RequestParam serviceKode: String?,
        @RequestParam serviceEdition: String?
    ): ResponseEntity<List<Organisasjon?>> {
        val fnr: String = FnrExtractor.extract(requestContextHolder)
        val result: List<Organisasjon?> =
            altinnService.hentOrganisasjonerBasertPaRettigheter(fnr, serviceKode!!, serviceEdition!!)!!
        return ResponseEntity.ok(result)
    }

}

