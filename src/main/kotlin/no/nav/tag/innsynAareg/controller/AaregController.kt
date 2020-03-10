package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.Protected
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.service.AaregService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@Protected
class AaregController (val resttemplate: RestTemplate, val aaregService:AaregService) {
    @GetMapping(value= ["/arbeidsforhold"])
    fun hentArbeidsforhold(): OversiktOverArbeidsForhold {
        return aaregService.hentArbeidsforhold("132","13");
    }
}