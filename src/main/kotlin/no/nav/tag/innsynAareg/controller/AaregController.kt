package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.Protected
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.service.InnsynService
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.annotations.ApiIgnore

@RestController
@Protected
class AaregController(val aAregService: InnsynService) {
    @GetMapping(value = ["/arbeidsforhold"])
    fun hentArbeidsforhold(
        @RequestHeader("orgnr") orgnr: String,
        @RequestHeader("jurenhet") juridiskEnhetOrgnr: String,
        @ApiIgnore @CookieValue("selvbetjening-idtoken") idToken: String
    ): OversiktOverArbeidsForhold {
        return aAregService.hentArbeidsforhold(orgnr, juridiskEnhetOrgnr, idToken)
    }

    @GetMapping(value = ["/antall-arbeidsforhold"])
    fun hentAntallArbeidsforhold(
        @RequestHeader("orgnr") orgnr: String,
        @RequestHeader("jurenhet") juridiskEnhetOrgnr: String,
        @ApiIgnore @CookieValue("selvbetjening-idtoken") idToken: String
    ): Pair<String, Number> {
        return aAregService.hentAntallArbeidsforholdPaUnderenhet(orgnr, juridiskEnhetOrgnr, idToken)
    }
}
