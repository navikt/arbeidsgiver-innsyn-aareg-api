package no.nav.tag.innsynAareg.controller

import lombok.extern.slf4j.Slf4j
import no.nav.security.token.support.core.api.Protected
import no.nav.tag.innsynAareg.models.altinn.Organisasjon
import no.nav.tag.innsynAareg.service.altinn.AltinnService
import no.nav.tag.innsynAareg.utils.FnrExtractor
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import no.nav.security.token.support.core.context.TokenValidationContextHolder

@Protected
@Slf4j
@RestController
class OrganisasjonController ( val altinnService: AltinnService, val requestContextHolder: TokenValidationContextHolder){

    @GetMapping(value = ["/api/organisasjoner"])
    fun hentOrganisasjoner(): ResponseEntity<List<Organisasjon>>? {
        val fnr: String = FnrExtractor.extract(requestContextHolder!!)
        val result: List<Organisasjon> = altinnService!!.hentOrganisasjoner(fnr)

        //no.nav.tag.dittNavArbeidsgiver.controller.OrganisasjonController.log.info("organisasjoner fra altinn:{}", result)
        return ResponseEntity.ok<List<Organisasjon>>(result)
    }

    @GetMapping(value = ["/api/rettigheter-til-skjema"])
    fun hentRettigheter(@RequestParam serviceKode: String?, @RequestParam serviceEdition: String?): ResponseEntity<List<Organisasjon>>? {
        val fnr: String = FnrExtractor.extract(requestContextHolder!!)
        val result: List<Organisasjon> = altinnService!!.hentOrganisasjonerBasertPaRettigheter(fnr, serviceKode!!, serviceEdition!!)
        return ResponseEntity.ok<List<Organisasjon>>(result)
    }

}

