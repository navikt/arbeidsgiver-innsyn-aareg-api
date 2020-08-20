package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.Protected
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.models.ArbeidsforholdFunnet
import no.nav.tag.innsynAareg.models.IngenRettigheter
import no.nav.tag.innsynAareg.service.InnsynService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    ): ResponseEntity<OversiktOverArbeidsForhold> =
        when (val respons = aAregService.hentArbeidsforhold(orgnr, juridiskEnhetOrgnr, idToken)) {
            is ArbeidsforholdFunnet -> ResponseEntity.ok(respons.oversiktOverArbeidsForhold)
            IngenRettigheter -> ResponseEntity(HttpStatus.FORBIDDEN)
        }

    @GetMapping(value = ["/antall-arbeidsforhold"])
    fun hentAntallArbeidsforhold(
        @RequestHeader("orgnr") orgnr: String,
        @RequestHeader("jurenhet") juridiskEnhetOrgnr: String,
        @ApiIgnore @CookieValue("selvbetjening-idtoken") idToken: String
    ): Pair<String, Number> {
        return aAregService.hentAntallArbeidsforholdPåUnderenhet(orgnr, juridiskEnhetOrgnr, idToken)
    }
}
