package no.nav.tag.innsynAareg.service

import no.nav.tag.innsynAareg.client.aareg.AaregClient
import no.nav.tag.innsynAareg.client.aareg.AaregException
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.OrganisasjonFraEreg
import no.nav.tag.innsynAareg.client.pdl.PdlBatchClient
import no.nav.tag.innsynAareg.client.pdl.dto.PdlBatchRespons
import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkClient
import no.nav.tag.innsynAareg.client.aareg.dto.ArbeidsForhold
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.client.altinn.AltinnClient
import no.nav.tag.innsynAareg.models.*
import no.nav.tag.innsynAareg.utils.SERVICEKODE_INNSYN_AAREG
import no.nav.tag.innsynAareg.utils.SERVICE_EDITION_INNSYN_AAREG
import no.nav.tag.innsynAareg.utils.withTimer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InnsynService(
    private val aaregClient: AaregClient,
    private val yrkeskodeverkClient: YrkeskodeverkClient,
    private val pdlBatchClient: PdlBatchClient,
    private val enhetsregisteretService: EnhetsregisteretClient,
    private val altinnClient: AltinnClient
) {
    private val logger = LoggerFactory.getLogger(InnsynService::class.java)!!

    fun hentAntallArbeidsforholdPåUnderenhet(
            orgnrUnderenhet: String,
            orgnrHovedenhet: String,
            idPortenToken: String
    ): Pair<String, Int> {
        val antall: Int? = aaregClient.antallArbeidsforholdForOpplysningspliktig(
                orgnrUnderenhet,
                orgnrHovedenhet,
                idPortenToken
        )

        if (antall != null && antall >= 0) {
            return Pair(orgnrHovedenhet, antall)
        }

        val orgtreFraEnhetsregisteret: OrganisasjonFraEreg =
                enhetsregisteretService.hentOrganisasjonFraEnhetsregisteret(orgnrUnderenhet, false)
                        ?: throw RuntimeException("enhetsregisteret frant ingen organisasjon med orgnummer $orgnrUnderenhet")

        if (orgtreFraEnhetsregisteret.bestaarAvOrganisasjonsledd.isNullOrEmpty()) {
            logger.info("Fant null arbeidsforhold for organisasjon: $orgnrUnderenhet")
            return Pair(orgnrHovedenhet, 0)
        }

        return try {
            itererOverOrgtre(
                    orgnrUnderenhet,
                    orgtreFraEnhetsregisteret.bestaarAvOrganisasjonsledd?.get(0)?.organisasjonsledd!!,
                    idPortenToken
            )
        } catch (exception: Exception) {
            logger.error("Klarte ikke itere over orgtre ", exception.message)
            throw AaregException(" Aareg Exception, klarte ikke finne opplysningspliktig: $exception")
        }
    }

    private fun itererOverOrgtre(
            orgnrUnderenhet: String,
            orgledd: OrganisasjonFraEreg,
            idToken: String
    ): Pair<String, Int> {
        val antall = aaregClient.antallArbeidsforholdForOpplysningspliktig(
                orgnrUnderenhet,
                orgledd.organisasjonsnummer,
                idToken
        )
        if (antall != null && antall != 0) {
            return Pair(orgledd.organisasjonsnummer, antall)
        } else if (orgledd.inngaarIJuridiskEnheter != null) {
            try {
                val juridiskEnhetOrgnr: String = orgledd.inngaarIJuridiskEnheter[0].organisasjonsnummer
                val antallNesteNiva = aaregClient.antallArbeidsforholdForOpplysningspliktig(
                        orgnrUnderenhet,
                        juridiskEnhetOrgnr,
                        idToken
                )
                return Pair(juridiskEnhetOrgnr, antallNesteNiva!!)
            } catch (exception: Exception) {
                throw AaregException(" Aareg Exception, feilet å finne antall arbeidsforhold på øverste nivå: $exception")
            }
        } else {
            return itererOverOrgtre(orgnrUnderenhet, orgledd.organisasjonsleddOver!![0].organisasjonsledd, idToken)
        }
    }

    fun hentArbeidsforhold(
            bedriftsnr: String,
            overOrdnetEnhetOrgnr: String,
            idPortenToken: String
    ): ArbeidsforholdOppslagResultat {
        val opplysningspliktigorgnr: String =
                try {
                    hentAntallArbeidsforholdPåUnderenhet(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken).first
                } catch (e: Exception) {
                    logger.warn("Exception. Bruker overordnet enhets orgnr fra http-request", e)
                    overOrdnetEnhetOrgnr
                }
        val arbeidsforhold = aaregClient.hentArbeidsforhold(bedriftsnr, opplysningspliktigorgnr, idPortenToken)
        if (arbeidsforhold is ArbeidsforholdFunnet) {
            settNavnPåArbeidsforholdBatch(arbeidsforhold.oversiktOverArbeidsForhold)
            settYrkeskodebetydningPaAlleArbeidsforhold(arbeidsforhold.oversiktOverArbeidsForhold)
        }
        return arbeidsforhold
    }


    fun hentTidligereArbeidsforhold(bedriftsnr: String,
                                    overOrdnetEnhetOrgnr: String,
                                    idPortenToken: String,
                                    fnr: String
    ): ArbeidsforholdOppslagResultat {

        var oversiktOverArbeidsforhold: ArbeidsforholdOppslagResultat;
        val arbeidsforholdGittOpplysningspliktig: ArbeidsforholdOppslagResultat = aaregClient.hentArbeidsforhold(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken)
        oversiktOverArbeidsforhold = arbeidsforholdGittOpplysningspliktig
        if (arbeidsforholdGittOpplysningspliktig !is ArbeidsforholdFunnet) {
            oversiktOverArbeidsforhold = finnOpplysningspliktigOgHentArbeidsforhold(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken, fnr)
        }
        if (oversiktOverArbeidsforhold is ArbeidsforholdFunnet) {
            settNavnPåArbeidsforholdBatch(oversiktOverArbeidsforhold.oversiktOverArbeidsForhold)
            settYrkeskodebetydningPaAlleArbeidsforhold(oversiktOverArbeidsforhold.oversiktOverArbeidsForhold)
        }
        return oversiktOverArbeidsforhold
    }


    fun finnOpplysningspliktigOgHentArbeidsforhold(
            bedriftsnr: String,
            overOrdnetEnhetOrgnr: String,
            idPortenToken: String,
            fnr: String
    ): ArbeidsforholdOppslagResultat {
        val organisasjonerMedTilgang = altinnClient.hentOrganisasjonerBasertPaRettigheter(fnr, SERVICEKODE_INNSYN_AAREG, SERVICE_EDITION_INNSYN_AAREG)
        if (organisasjonerMedTilgang is AltinnOppslagVellykket) {
            val juridiskeEnhetermedTilgang = organisasjonerMedTilgang.organisasjoner.filter { it.Type == "Enterprise" }
            juridiskeEnhetermedTilgang.forEach {
                try {
                    val arbeidsforhold = aaregClient.hentArbeidsforhold(bedriftsnr, it.OrganizationNumber!!, idPortenToken)
                    if (arbeidsforhold is ArbeidsforholdFunnet) {
                        logger.info("Klarte finne historiske arbeidsforhold for $bedriftsnr og ${it.OrganizationNumber}")
                        return arbeidsforhold
                    }
                } catch (e: Exception) {
                    logger.info("Spurte Aareg etter arbeidsforhold på kombinasjonen $bedriftsnr og ${it.OrganizationNumber} i historiske arbeidsforhold")
                }
            }
            logger.info("Klarte ikke hente historiske arbeidsforhold fra aareg. juridiskeEnhetermedTilgang: $juridiskeEnhetermedTilgang")
        }
        return IngenRettigheter
    }

    private fun settYrkeskodebetydningPaAlleArbeidsforhold(
            arbeidsforholdOversikt: OversiktOverArbeidsForhold
    ) = withTimer("DittNavArbeidsgiverApi.hentYrker") {
        val yrkeskodeBeskrivelser: Yrkeskoder = yrkeskodeverkClient.hentBetydningAvYrkeskoder()
        for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter!!) {
            arbeidsforhold.yrkesbeskrivelse = yrkeskodeBeskrivelser.betydningPåYrke(arbeidsforhold.yrke)
        }
    }

    companion object {
        const val BATCH_SIZE = 100
    }

    private fun settNavnPåArbeidsforholdBatch(arbeidsforholdOversikt: OversiktOverArbeidsForhold) {
        arbeidsforholdOversikt
                .arbeidsforholdoversikter
                ?.asIterable()
                ?.windowed(size = BATCH_SIZE, step = BATCH_SIZE, partialWindows = true)
                ?.forEach(::settNavnPåArbeidsforholdSingleBatch)
    }

    private fun settNavnPåArbeidsforholdSingleBatch(
            arbeidsforholdOversikt: List<ArbeidsForhold>
    ) {
        val fnrs = arbeidsforholdOversikt.mapNotNull {
            it.arbeidstaker.offentligIdent
        }
        val respons: PdlBatchRespons? = pdlBatchClient.getBatchFraPdl(fnrs)
        if (respons !== null) {
            for (person in respons.data.hentPersonBolk) {
                for (arbeidsforhold in arbeidsforholdOversikt) {
                    if (person.ident == arbeidsforhold.arbeidstaker.offentligIdent) {
                        if (person.code != "ok") {
                            logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn  {}", person.code)
                        }
                        val navn = person.person?.navn?.getOrNull(0);
                        if (navn === null) {
                            logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn, ukjent grunn")
                            arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn"
                        } else {
                            arbeidsforhold.arbeidstaker.navn =
                                    listOfNotNull(navn.fornavn, navn.mellomNavn, navn.etternavn)
                                            .joinToString(" ")
                        }
                    }
                }

        }
            for (arbeidsforhold in arbeidsforholdOversikt) {
                if (arbeidsforhold.arbeidstaker.navn.isNullOrBlank()) {
                    arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn"
                }
            }
        }
    }
}

