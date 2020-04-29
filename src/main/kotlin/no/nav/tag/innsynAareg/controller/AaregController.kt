package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.Protected
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.service.AaregService
import no.nav.tag.innsynAareg.service.enhetsregisteret.EnhetsregisterService
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import springfox.documentation.annotations.ApiIgnore

@RestController
@Protected
class AaregController (val resttemplate: RestTemplate, val aAregService:AaregService) {
    @GetMapping(value = ["/arbeidsforhold"])
    fun hentArbeidsforhold(@RequestHeader("orgnr") orgnr: String,
                           @RequestHeader("jurenhet") juridiskEnhetOrgnr: String,
                           @ApiIgnore @CookieValue("selvbetjening-idtoken") idToken: String): OversiktOverArbeidsForhold? {
        val response: OversiktOverArbeidsForhold? = aAregService.hentArbeidsforhold(orgnr, juridiskEnhetOrgnr, idToken);
        return response
    }

    @GetMapping(value = ["/antall-arbeidsforhold"])
    fun hentAntallArbeidsforhold(@RequestHeader("orgnr") orgnr: String,
                           @RequestHeader("jurenhet") juridiskEnhetOrgnr: String,
                           @ApiIgnore @CookieValue("selvbetjening-idtoken") idToken: String): Pair<String, Number> {
        val response: Pair<String, Number> = aAregService.hentAntallArbeidsforholdPaUnderenhet(orgnr, juridiskEnhetOrgnr, idToken);
        return response
    }
}

