package no.nav.tag.innsynAareg.service

import lombok.extern.slf4j.Slf4j
import no.nav.metrics.MetricsFactory
import no.nav.metrics.Timer
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.models.Yrkeskoderespons.Yrkeskoderespons
import no.nav.tag.innsynAareg.service.pdl.PdlService
import no.nav.tag.innsynAareg.service.sts.STSClient
import no.nav.tag.innsynAareg.service.yrkeskoder.YrkeskodeverkService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Slf4j
@Service
class AaregService (val restTemplate: RestTemplate, val stsClient: STSClient,val yrkeskodeverkService: YrkeskodeverkService ,val pdlService: PdlService){
    @Value("\${aareg.aaregArbeidsforhold}")
    lateinit var aaregArbeidsforholdUrl: String
    val logger = LoggerFactory.getLogger(YrkeskodeverkService::class.java)
    fun hentArbeidsforhold(bedriftsnr:String, overOrdnetEnhetOrgnr:String,idPortenToken: String):OversiktOverArbeidsForhold {
        val kunArbeidstimer: Timer= MetricsFactory.createTimer("DittNavArbeidsgiverApi.kunArbeidsforhold").start()
        val arbeidsforhold = hentArbeidsforholdFraAAReg(bedriftsnr,overOrdnetEnhetOrgnr,idPortenToken)
        kunArbeidstimer.stop().report()
        val arbeidsforholdMedNavn = settNavnPaArbeidsforhold(arbeidsforhold);
        return settYrkeskodebetydningPaAlleArbeidsforhold(arbeidsforholdMedNavn!!)!!
    }

    fun hentArbeidsforholdFraAAReg(bedriftsnr:String, overOrdnetEnhetOrgnr:String,idPortenToken: String):OversiktOverArbeidsForhold {
        val url = aaregArbeidsforholdUrl
        val entity: HttpEntity<String> = getRequestEntity(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken)
        return try {
            val respons = restTemplate.exchange(url,
                    HttpMethod.GET, entity, OversiktOverArbeidsForhold::class.java)
            if (respons.statusCode != HttpStatus.OK) {
                val message = "Kall mot aareg feiler med HTTP-" + respons.statusCode
                throw RuntimeException(message)
            }
            respons.body!!
        } catch (exception: RestClientException) {
            throw RuntimeException(" Aareg Exception: $exception")
        }
    }

    private fun getRequestEntity(bedriftsnr: String, juridiskEnhetOrgnr: String, idPortenToken: String): HttpEntity<String> {
        val appName = "srvditt-nav-arbeid"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers["Authorization"] = "Bearer $idPortenToken"
        headers["Nav-Call-Id"] = appName
        headers["Nav-Arbeidsgiverident"] = bedriftsnr
        headers["Nav-Opplysningspliktigident"] = juridiskEnhetOrgnr
        headers["Nav-Consumer-Token"] = stsClient.token?.access_token;
        return HttpEntity(headers)
    }
    fun settYrkeskodebetydningPaAlleArbeidsforhold(arbeidsforholdOversikt: OversiktOverArbeidsForhold): OversiktOverArbeidsForhold? {
        val hentYrkerTimer: Timer = MetricsFactory.createTimer("DittNavArbeidsgiverApi.hentYrker").start()
        val yrkeskodeBeskrivelser: Yrkeskoderespons = yrkeskodeverkService.hentBetydningerAvYrkeskoder()!!
        for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter) {
            val yrkeskode: String = arbeidsforhold.yrke
            val yrkeskodeBeskrivelse: String = finnYrkeskodebetydningPaYrke(yrkeskode, yrkeskodeBeskrivelser)!!
            arbeidsforhold.yrkesbeskrivelse =yrkeskodeBeskrivelse
        }
        hentYrkerTimer.stop().report()
         return arbeidsforholdOversikt
    }
    fun finnYrkeskodebetydningPaYrke(yrkeskodenokkel: String?, yrkeskoderespons: Yrkeskoderespons): String? {
        return yrkeskoderespons.betydninger.get(yrkeskodenokkel)?.get(0)?.beskrivelser?.nb?.tekst
    }

    fun settNavnPaArbeidsforhold(arbeidsforholdOversikt: OversiktOverArbeidsForhold): OversiktOverArbeidsForhold? {
        if (!arbeidsforholdOversikt.arbeidsforholdoversikter.isNullOrEmpty()) {
            for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter) {
                val fnr: String = arbeidsforhold.arbeidstaker.offentligIdent;
                if (!fnr.isBlank()) {
                    val navnPaArbeidstaker: String = pdlService.hentNavnMedFnr(fnr)
                    arbeidsforhold.arbeidstaker.navn = navnPaArbeidstaker;
                }
            }
        }
        return arbeidsforholdOversikt
    }

}