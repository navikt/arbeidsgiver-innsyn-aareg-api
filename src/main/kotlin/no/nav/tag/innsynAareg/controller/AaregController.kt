package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.Protected
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.models.enhetsregisteret.EnhetsRegisterOrg
import no.nav.tag.innsynAareg.models.enhetsregisteret.Organisasjoneledd
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
class AaregController (val resttemplate: RestTemplate, val aAregService:AaregService, val enhetsregisteretService: EnhetsregisterService) {
    @GetMapping(value= ["/arbeidsforhold"])
    fun hentArbeidsforhold(@RequestHeader("orgnr") orgnr:String,
                           @RequestHeader("jurenhet") juridiskEnhetOrgnr:String ,
                           @ApiIgnore @CookieValue("selvbetjening-idtoken") idToken:String): OversiktOverArbeidsForhold? {
        var response: OversiktOverArbeidsForhold? = aAregService.hentArbeidsforhold(orgnr,juridiskEnhetOrgnr,idToken);
        if (response?.arbeidsforholdoversikter.isNullOrEmpty()) {
            return finnOpplysningspliktigorg(orgnr, idToken)!!
        }
        return response
    }



    fun finnOpplysningspliktigorg(orgnr: String, idToken: String?): OversiktOverArbeidsForhold? {
        val orgtreFraEnhetsregisteret: EnhetsRegisterOrg? = enhetsregisteretService.hentOrgnaisasjonFraEnhetsregisteret(orgnr)
        //no.nav.tag.dittNavArbeidsgiver.controller.AAregController.log.info("MSA-AAREG finnOpplysningspliktigorg, orgtreFraEnhetsregisteret: $orgtreFraEnhetsregisteret")
        return if (!orgtreFraEnhetsregisteret?.bestaarAvOrganisasjonsledd.isNullOrEmpty()) {
            itererOverOrgtre(orgnr, orgtreFraEnhetsregisteret!!.bestaarAvOrganisasjonsledd.get(0).organisasjonsledd!!, idToken)
        } else return null;
    }

    fun itererOverOrgtre(orgnr: String, orgledd: Organisasjoneledd, idToken: String?): OversiktOverArbeidsForhold? {
        val result: OversiktOverArbeidsForhold = aAregService.hentArbeidsforhold(orgnr, orgledd!!.organisasjonsnummer, idToken)
        //no.nav.tag.dittNavArbeidsgiver.controller.AAregController.log.info("MSA-AAREG itererOverOrgtre orgnr: " + orgnr + "orgledd: " + orgledd)
         if (result.arbeidsforholdoversikter.isNotEmpty()) {
             return result
        } else if (orgledd.inngaarIJuridiskEnheter != null) {
            val juridiskEnhetOrgnr: String? = orgledd.inngaarIJuridiskEnheter?.get(0)!!.organisasjonsnummer
            //no.nav.tag.dittNavArbeidsgiver.controller.AAregController.log.info("MSA-AAREG itererOverOrgtre orgnr: " + orgnr + "juridiskEnhetOrgnr: " + juridiskEnhetOrgnr)
            return aAregService.hentArbeidsforhold(orgnr, juridiskEnhetOrgnr, idToken)
        } else {
            return itererOverOrgtre(orgnr, orgledd.organisasjonsleddOver!![0].organisasjonsledd!!, idToken)
        }
    }

}