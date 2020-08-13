package no.nav.tag.innsynAareg.service

import no.nav.metrics.MetricsFactory
import no.nav.metrics.Timer
import no.nav.tag.innsynAareg.client.aareg.AaregClient
import no.nav.tag.innsynAareg.client.aareg.AaregException
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.EnhetsRegisterOrg
import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.Organisasjoneledd
import no.nav.tag.innsynAareg.client.pdl.PdlBatchClient
import no.nav.tag.innsynAareg.client.pdl.dto.Navn
import no.nav.tag.innsynAareg.client.pdl.dto.PdlBatchRespons
import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkClient
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsgiver
import no.nav.tag.innsynAareg.models.yrkeskoder.Yrkeskoderespons
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class InnsynService(
    val aaregClient: AaregClient,
    val yrkeskodeverkClient: YrkeskodeverkClient,
    val pdlBatchClient: PdlBatchClient,
    val enhetsregisteretService: EnhetsregisteretClient
) {

    val logger = LoggerFactory.getLogger(InnsynService::class.java)!!

    //Kode for nøsting basert på antall-kall
    fun hentAntallArbeidsforholdPaUnderenhet(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String,
        idPortenToken: String
    ): Pair<String, Int> {
        //respons er tomt array dersom det er feil opplysningpliktig
        val respons: Array<OversiktOverArbeidsgiver> =
            aaregClient.hentOVersiktOverAntallArbeidsforholdForOpplysningspliktigFraAAReg(
                bedriftsnr,
                overOrdnetEnhetOrgnr,
                idPortenToken
            )
        return finnAntallArbeidsforholdPaUnderenhet(bedriftsnr, respons, overOrdnetEnhetOrgnr, idPortenToken)
    }

    fun hentArbeidsforhold(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String,
        idPortenToken: String
    ): OversiktOverArbeidsForhold {
        val opplysningspliktigorgnr: String =
            hentAntallArbeidsforholdPaUnderenhet(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken).first
        val arbeidsforhold = aaregClient.hentArbeidsforholdFraAAReg(bedriftsnr, opplysningspliktigorgnr, idPortenToken)
        return settPaNavnOgYrkesbeskrivelse(arbeidsforhold)
    }


    private fun settPaNavnOgYrkesbeskrivelse(arbeidsforhold: OversiktOverArbeidsForhold): OversiktOverArbeidsForhold {
        val arbeidsforholdMedNavn = settNavnPåArbeidsforholdBatch(arbeidsforhold)
        return settYrkeskodebetydningPaAlleArbeidsforhold(arbeidsforholdMedNavn)
    }

    private fun settYrkeskodebetydningPaAlleArbeidsforhold(
        arbeidsforholdOversikt: OversiktOverArbeidsForhold
    ): OversiktOverArbeidsForhold {
        val hentYrkerTimer: Timer = MetricsFactory.createTimer("DittNavArbeidsgiverApi.hentYrker").start()
        val yrkeskodeBeskrivelser: Yrkeskoderespons = yrkeskodeverkClient.hentBetydningerAvYrkeskoder()!!
        for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter!!) {
            val yrkeskode: String = arbeidsforhold.yrke
            val yrkeskodeBeskrivelse: String = finnYrkeskodebetydningPaYrke(yrkeskode, yrkeskodeBeskrivelser)!!
            arbeidsforhold.yrkesbeskrivelse = yrkeskodeBeskrivelse
        }
        hentYrkerTimer.stop().report()
        return arbeidsforholdOversikt
    }

    private fun finnYrkeskodebetydningPaYrke(yrkeskodenokkel: String?, yrkeskoderespons: Yrkeskoderespons): String? {
        try {
            return yrkeskoderespons.betydninger[yrkeskodenokkel]!![0].beskrivelser!!.nb!!.tekst
        } catch (e: Exception) {
            logger.error("Fant ikke betydning for yrkeskode: {}", yrkeskodenokkel, e.message)
        }
        return "Fant ikke yrkesbeskrivelse"
    }

    private fun settNavnPåArbeidsforholdMedBatchMaxHundre(
        arbeidsforholdOversikt: OversiktOverArbeidsForhold,
        fnrs: List<String>
    ) {
        val maksHundreFnrs = fnrs.toTypedArray()
        val respons: PdlBatchRespons = pdlBatchClient.getBatchFraPdl(maksHundreFnrs)!!
        for (i in 0 until respons.data.hentPersonBolk.size) {
            for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter!!) {
                if (respons.data.hentPersonBolk[i].ident.equals(arbeidsforhold.arbeidstaker.offentligIdent)) {
                    try {
                        val navnObjekt: Navn = respons.data.hentPersonBolk[i].person!!.navn!![0]
                        var navn = ""
                        if (navnObjekt.fornavn != null) navn += navnObjekt.fornavn
                        if (navnObjekt.mellomNavn != null) navn += " " + navnObjekt.mellomNavn
                        if (navnObjekt.etternavn != null) navn += " " + navnObjekt.etternavn
                        arbeidsforhold.arbeidstaker.navn = navn
                    } catch (e: NullPointerException) {
                        logger.error("AG-ARBEIDSFORHOLD PDL ERROR nullpointer exception ", e.message)
                        if (respons.data.hentPersonBolk[i].code != "ok") {
                            logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn  " + respons.data.hentPersonBolk[i].code)
                        } else {
                            logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn, ukjent grunn")
                        }
                        arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn"
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        logger.error(
                            "AG-ARBEIDSFORHOLD PDL ERROR fant ikke person i respons " + respons.data.hentPersonBolk[i].code,
                            e.message
                        )
                        arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn"
                    }
                }
            }
        }
    }

    private fun settNavnPåArbeidsforholdBatch(
        arbeidsforholdOversikt: OversiktOverArbeidsForhold
    ): OversiktOverArbeidsForhold {
        val lengde: Int = arbeidsforholdOversikt.arbeidsforholdoversikter!!.size
        val fnrs = ArrayList<String>(lengde)
        for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter) {
            fnrs.add(arbeidsforhold.arbeidstaker.offentligIdent)
        }
        var tempStartIndeks = 0
        var gjenVarendelengde = lengde
        while (gjenVarendelengde > 100) {
            settNavnPåArbeidsforholdMedBatchMaxHundre(
                arbeidsforholdOversikt,
                fnrs.subList(tempStartIndeks, tempStartIndeks + 100)
            )
            tempStartIndeks += 100
            gjenVarendelengde -= 100
        }
        if (gjenVarendelengde > 0) {
            settNavnPåArbeidsforholdMedBatchMaxHundre(arbeidsforholdOversikt, fnrs.subList(tempStartIndeks, lengde))
        }
        return arbeidsforholdOversikt
    }


    private fun finnAntallArbeidsforholdPaUnderenhet(
        bedriftsnr: String,
        oversikt: Array<OversiktOverArbeidsgiver>,
        juridiskEnhetOrgnr: String,
        idPortenToken: String
    ): Pair<String, Int> {
        val antall: Int? = finnAntallGittListe(bedriftsnr, oversikt)
        return if (antall != null && antall >= 0) {
            Pair(juridiskEnhetOrgnr, antall)
        } else {
            finnOpplysningspliktigOrgOgAntallAnsatte(bedriftsnr, idPortenToken, juridiskEnhetOrgnr)
        }
    }

    private fun finnAntallGittListe(orgnr: String, oversikt: Array<OversiktOverArbeidsgiver>): Int? {
        if (oversikt.isEmpty()) {
            logger.info("Aareg oversikt over arbeidsgiver respons er tom for orgnr: $orgnr")
        }

        val valgUnderenhetOVersikt: OversiktOverArbeidsgiver? =
            oversikt.find { it.arbeidsgiver.organisasjonsnummer == orgnr }

        if (valgUnderenhetOVersikt != null) {
            return valgUnderenhetOVersikt.aktiveArbeidsforhold + valgUnderenhetOVersikt.inaktiveArbeidsforhold
        }
        return null
    }

    private fun finnOpplysningspliktigOrgOgAntallAnsatte(
        orgnr: String,
        idToken: String,
        juridiskEnhetsNr: String
    ): Pair<String, Int> {
        val orgtreFraEnhetsregisteret: EnhetsRegisterOrg? =
            enhetsregisteretService.hentOrgnaisasjonFraEnhetsregisteret(orgnr)
        if (orgtreFraEnhetsregisteret!!.bestaarAvOrganisasjonsledd.isNullOrEmpty()) {
            logger.info("Fant null arbeidsforhold for organisasjon: $orgnr")
            return Pair(juridiskEnhetsNr, 0)
        }
        return try {
            itererOverOrgtre(
                orgnr,
                orgtreFraEnhetsregisteret.bestaarAvOrganisasjonsledd?.get(0)?.organisasjonsledd!!,
                idToken
            )
        } catch (exception: Exception) {
            logger.error("Klarte ikke itere over orgtre ", exception.message)
            throw AaregException(" Aareg Exception, klarte ikke finne opplysningspliktig: $exception")
        }
    }

    private fun itererOverOrgtre(orgnr: String, orgledd: Organisasjoneledd, idToken: String): Pair<String, Int> {
        val oversikt = aaregClient.hentOVersiktOverAntallArbeidsforholdForOpplysningspliktigFraAAReg(
            orgnr,
            orgledd.organisasjonsnummer,
            idToken
        )
        val antall = finnAntallGittListe(orgnr, oversikt)
        if (antall != null && antall != 0) {
            return Pair(orgledd.organisasjonsnummer!!, antall)
        } else if (orgledd.inngaarIJuridiskEnheter != null) {
            try {
                val juridiskEnhetOrgnr: String = orgledd.inngaarIJuridiskEnheter?.get(0)!!.organisasjonsnummer!!
                val oversiktNesteNiva = aaregClient.hentOVersiktOverAntallArbeidsforholdForOpplysningspliktigFraAAReg(
                    orgnr,
                    juridiskEnhetOrgnr,
                    idToken
                )
                val antallNesteNiva = finnAntallGittListe(orgnr, oversiktNesteNiva)
                return Pair(juridiskEnhetOrgnr, antallNesteNiva!!)
            } catch (exception: Exception) {
                throw AaregException(" Aareg Exception, feilet å finne antall arbeidsforhold på øverste nivå: $exception")
            }
        }
        return itererOverOrgtre(orgnr, orgledd.organisasjonsleddOver!![0].organisasjonsledd!!, idToken)
    }
}
