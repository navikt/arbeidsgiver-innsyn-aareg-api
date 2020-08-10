package no.nav.tag.innsynAareg.service.aareg

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import lombok.extern.slf4j.Slf4j
import no.nav.metrics.MetricsFactory
import no.nav.metrics.Timer
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsgiver
import no.nav.tag.innsynAareg.models.Yrkeskoderespons.Yrkeskoderespons
import no.nav.tag.innsynAareg.models.enhetsregisteret.EnhetsRegisterOrg
import no.nav.tag.innsynAareg.models.enhetsregisteret.Organisasjoneledd
import no.nav.tag.innsynAareg.models.pdlBatch.PdlBatchRespons
import no.nav.tag.innsynAareg.models.pdlBatch.Navn
import no.nav.tag.innsynAareg.service.enhetsregisteret.EnhetsregisterService
import no.nav.tag.innsynAareg.service.pdl.PdlBatchService
import no.nav.tag.innsynAareg.service.sts.STSClient
import no.nav.tag.innsynAareg.service.yrkeskoder.YrkeskodeverkService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*
import kotlin.system.measureTimeMillis

@Slf4j
@Service
class AaregService (val restTemplate: RestTemplate, val stsClient: STSClient,val yrkeskodeverkService: YrkeskodeverkService, val pdlBatchService: PdlBatchService, val enhetsregisteretService: EnhetsregisterService){
    @Value("\${aareg.aaregArbeidsforhold}")
    lateinit var aaregArbeidsforholdUrl: String
    @Value("\${aareg.aaregArbeidsgivere}")
    lateinit var aaregArbeidsgiverOversiktUrl: String
    val logger = LoggerFactory.getLogger(AaregService::class.java)

    fun hentArbeidsforhold(bedriftsnr:String, overOrdnetEnhetOrgnr:String,idPortenToken: String?):OversiktOverArbeidsForhold {
        val opplysningspliktigorgnr: String? = hentAntallArbeidsforholdPaUnderenhet(bedriftsnr, overOrdnetEnhetOrgnr,idPortenToken!!).first
        val arbeidsforhold = hentArbeidsforholdFraAAReg(bedriftsnr,opplysningspliktigorgnr,idPortenToken)
        return settPaNavnOgYrkesbeskrivelse(arbeidsforhold)!!;
    }

    fun hentArbeidsforholdFraAAReg(bedriftsnr:String, overOrdnetEnhetOrgnr:String?,idPortenToken: String?):OversiktOverArbeidsForhold {
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
            logger.error("Feil ved oppslag mot Aareg Arbeidsforhold: ", exception.message)
            throw RuntimeException(" Aareg Exception: $exception")
        }
    }

    fun settPaNavnOgYrkesbeskrivelse(arbeidsforhold :OversiktOverArbeidsForhold): OversiktOverArbeidsForhold?{
        val arbeidsforholdMedNavn = settNavnPåArbeidsforholdBatch(arbeidsforhold);
        return settYrkeskodebetydningPaAlleArbeidsforhold(arbeidsforholdMedNavn!!)!!
    }



    private fun getRequestEntity(bedriftsnr: String, juridiskEnhetOrgnr: String?, idPortenToken: String?): HttpEntity<String> {
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
        for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter!!) {
            val yrkeskode: String = arbeidsforhold.yrke
            val yrkeskodeBeskrivelse: String = finnYrkeskodebetydningPaYrke(yrkeskode, yrkeskodeBeskrivelser)!!
            arbeidsforhold.yrkesbeskrivelse =yrkeskodeBeskrivelse
        }
        hentYrkerTimer.stop().report()
         return arbeidsforholdOversikt
    }
    fun finnYrkeskodebetydningPaYrke(yrkeskodenokkel: String?, yrkeskoderespons: Yrkeskoderespons): String? {
        try {
            val betydning = yrkeskoderespons.betydninger.get(yrkeskodenokkel)!!.get(0).beskrivelser!!.nb!!.tekst;
            return betydning;
        }
        catch (e: Exception) {
            logger.error("Fant ikke betydning for yrkeskode: "+ yrkeskodenokkel, e.message)
        }
        return "Fant ikke yrkesbeskrivelse"
    }

    fun settNavnPåArbeidsforholdMedBatchMaxHundre(arbeidsforholdOversikt: OversiktOverArbeidsForhold, fnrs: List<String>) {
        val maksHundreFnrs = fnrs.toTypedArray()
        val respons: PdlBatchRespons = pdlBatchService.getBatchFraPdl(maksHundreFnrs)!!
        for (i in 0 until respons.data.hentPersonBolk.size) {
            for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter!!) {
                if (respons.data.hentPersonBolk.get(i).ident.equals(arbeidsforhold.arbeidstaker.offentligIdent)) {
                    try {
                        val navnObjekt: Navn = respons.data.hentPersonBolk[i].person!!.navn!![0]
                        var navn = ""
                        if (navnObjekt.fornavn != null) navn += navnObjekt.fornavn
                        if (navnObjekt.mellomNavn != null) navn += " " + navnObjekt.mellomNavn
                        if (navnObjekt.etternavn != null) navn += " " + navnObjekt.etternavn
                        arbeidsforhold.arbeidstaker.navn = navn;
                    } catch (e: NullPointerException) {
                        logger.error("AG-ARBEIDSFORHOLD PDL ERROR nullpointer exception ", e.message);
                        if (respons.data.hentPersonBolk[i].code != "ok") {
                            logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn  " + respons.data.hentPersonBolk[i].code);
                        } else {
                            logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn, ukjent grunn");
                        }
                        arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn";
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke person i respons " + respons.data.hentPersonBolk[i].code, e.message);
                        arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn";
                    }
                }
            }
        }
    }

    fun settNavnPåArbeidsforholdBatch(arbeidsforholdOversikt: OversiktOverArbeidsForhold): OversiktOverArbeidsForhold? {
        val lengde: Int = arbeidsforholdOversikt.arbeidsforholdoversikter!!.size;
        val fnrs = ArrayList<String>(lengde)
        for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter) {
            fnrs.add(arbeidsforhold.arbeidstaker.offentligIdent)
        }
        var tempStartIndeks = 0
        var gjenVarendelengde = lengde
        while (gjenVarendelengde > 100) {
            settNavnPåArbeidsforholdMedBatchMaxHundre(arbeidsforholdOversikt, fnrs.subList(tempStartIndeks, tempStartIndeks + 100))
            tempStartIndeks = tempStartIndeks + 100
            gjenVarendelengde = gjenVarendelengde - 100
        }
        if (gjenVarendelengde > 0) {
            settNavnPåArbeidsforholdMedBatchMaxHundre(arbeidsforholdOversikt, fnrs.subList(tempStartIndeks, lengde))
        }
        return arbeidsforholdOversikt
    }

    //Kode for nøsting basert på antall-kall
    fun hentAntallArbeidsforholdPaUnderenhet(bedriftsnr:String, overOrdnetEnhetOrgnr:String,idPortenToken: String):Pair<String, Int> {
        //respons er tomt array dersom det er feil opplysningpliktig
        val respons: Array<OversiktOverArbeidsgiver> = hentOVersiktOverAntallArbeidsforholdForOpplysningspliktigFraAAReg(bedriftsnr, overOrdnetEnhetOrgnr,idPortenToken);
        return finnAntallArbeidsforholdPaUnderenhet(bedriftsnr, respons, overOrdnetEnhetOrgnr, idPortenToken);
    }

    fun hentOVersiktOverAntallArbeidsforholdForOpplysningspliktigFraAAReg(bedriftsnr:String, overOrdnetEnhetOrgnr:String?,idPortenToken: String):Array<OversiktOverArbeidsgiver> {
        val url = aaregArbeidsgiverOversiktUrl
        val entity: HttpEntity<String> = getRequestEntity(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken)
        return try {
            val respons = restTemplate.exchange(url,
                    HttpMethod.GET, entity, Array<OversiktOverArbeidsgiver>::class.java)
            if (respons.statusCode != HttpStatus.OK) {
                val message = "Kall mot aareg feiler med HTTP-" + respons.statusCode
                throw RuntimeException(message)
            }
            respons.body!!
        } catch (exception: RestClientException) {
            logger.error("Feiler ved å hente arbeidsgiveroversikt fra Aareg ", exception.message)
            throw RuntimeException(" Aareg Exception: $exception")
        }
    }

    fun finnAntallArbeidsforholdPaUnderenhet(bedriftsnr:String, oversikt: Array<OversiktOverArbeidsgiver>, juridiskEnhetOrgnr: String, idPortenToken: String): Pair<String, Int> {
        val antall: Int? = finnAntallGittListe(bedriftsnr,oversikt);
        return if (antall != null && antall>=0) {
            Pair(juridiskEnhetOrgnr, antall);
        }
        else {
            finnOpplysningspliktigOrgOgAntallAnsatte(bedriftsnr, idPortenToken)
        }
    }

    fun finnAntallGittListe(orgnr: String, oversikt: Array<OversiktOverArbeidsgiver>): Int? {
        val valgUnderenhetOVersikt: OversiktOverArbeidsgiver?  = oversikt.find { it.arbeidsgiver.organisasjonsnummer == orgnr };
        if (valgUnderenhetOVersikt != null) {
            return valgUnderenhetOVersikt.aktiveArbeidsforhold + valgUnderenhetOVersikt.inaktiveArbeidsforhold;
        }
        return null;
    }

    fun finnOpplysningspliktigOrgOgAntallAnsatte(orgnr: String, idToken: String): Pair<String, Int> {
        val orgtreFraEnhetsregisteret: EnhetsRegisterOrg? = enhetsregisteretService.hentOrgnaisasjonFraEnhetsregisteret(orgnr)
        return try {
            itererOverOrgtre(orgnr, orgtreFraEnhetsregisteret!!.bestaarAvOrganisasjonsledd[0].organisasjonsledd!!, idToken)
        }
        catch (exception: Exception) {
            logger.error("Klarte ikke itere over orgtre ", exception.message)

            throw AaregException(" Aareg Exception, klarte ikke finne opplysningspliktig: $exception")
        }
    }

    fun itererOverOrgtre(orgnr: String, orgledd: Organisasjoneledd, idToken: String): Pair<String, Int> {
        val oversikt  = hentOVersiktOverAntallArbeidsforholdForOpplysningspliktigFraAAReg(orgnr, orgledd.organisasjonsnummer, idToken)
        val antall = finnAntallGittListe(orgnr, oversikt);
        if (antall != null && antall != 0) {
            return Pair(orgledd.organisasjonsnummer!!, antall)
        }
        else if (orgledd.inngaarIJuridiskEnheter != null) {
             try {
                 val juridiskEnhetOrgnr: String? = orgledd.inngaarIJuridiskEnheter?.get(0)!!.organisasjonsnummer!!
                 val oversiktNesteNiva = hentOVersiktOverAntallArbeidsforholdForOpplysningspliktigFraAAReg(orgnr, juridiskEnhetOrgnr, idToken);
                 val antallNesteNiva = finnAntallGittListe(orgnr, oversiktNesteNiva);
                 return Pair(juridiskEnhetOrgnr!!, antallNesteNiva!!);
             }
             catch (exception: Exception) {
                 throw AaregException(" Aareg Exception, feilet å finne antall arbeidsforhold på øverste nivå: $exception")
             }
        }
        return itererOverOrgtre(orgnr, orgledd.organisasjonsleddOver!!.get(0).organisasjonsledd!!, idToken)
    }
};