package no.nav.tag.innsynAareg.service

import no.nav.tag.innsynAareg.client.aareg.AaregClient
import no.nav.tag.innsynAareg.client.aareg.AaregException
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.EnhetsRegisterOrg
import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.Organisasjoneledd
import no.nav.tag.innsynAareg.client.pdl.PdlBatchClient
import no.nav.tag.innsynAareg.client.pdl.dto.PdlBatchRespons
import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkClient
import no.nav.tag.innsynAareg.client.aareg.dto.ArbeidsForhold
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.models.ArbeidsforholdFunnet
import no.nav.tag.innsynAareg.models.ArbeidsforholdOppslagResultat
import no.nav.tag.innsynAareg.models.Yrkeskoder
import no.nav.tag.innsynAareg.utils.withTimer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class InnsynService(
    private val aaregClient: AaregClient,
    private val yrkeskodeverkClient: YrkeskodeverkClient,
    private val pdlBatchClient: PdlBatchClient,
    private val enhetsregisteretService: EnhetsregisteretClient
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

        val orgtreFraEnhetsregisteret: EnhetsRegisterOrg =
            enhetsregisteretService.hentOrgnaisasjonFraEnhetsregisteret(orgnrUnderenhet)
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
        orgledd: Organisasjoneledd,
        idToken: String
    ): Pair<String, Int> {
        val antall = aaregClient.antallArbeidsforholdForOpplysningspliktig(
            orgnrUnderenhet,
            orgledd.organisasjonsnummer,
            idToken
        )
        if (antall != null && antall != 0) {
            return Pair(orgledd.organisasjonsnummer!!, antall)
        } else if (orgledd.inngaarIJuridiskEnheter != null) {
            try {
                val juridiskEnhetOrgnr: String = orgledd.inngaarIJuridiskEnheter?.get(0)!!.organisasjonsnummer!!
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
            return itererOverOrgtre(orgnrUnderenhet, orgledd.organisasjonsleddOver!![0].organisasjonsledd!!, idToken)
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
        val respons: PdlBatchRespons = pdlBatchClient.getBatchFraPdl(
            arbeidsforholdOversikt.map { it.arbeidstaker.offentligIdent }
        ) ?: run {
            logger.error("getBatchFraPdl feilet")
            return
        }

        /* Litt uheldig å ha N^2 iterasjoner når det kan være N log(N). */
        for (person in respons.data.hentPersonBolk) {
            for (arbeidsforhold in arbeidsforholdOversikt) {
                if (person.ident == arbeidsforhold.arbeidstaker.offentligIdent) {
                    if (person.code != "ok") {
                        logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn  {}", person.code)
                        /* men vi kan jo prøve allikevel ... */
                    }

                    val navn = person.person?.navn?.getOrNull(0) ?: run {
                        logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn, ukjent grunn")
                        arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn"
                        return
                    }

                    arbeidsforhold.arbeidstaker.navn =
                        listOfNotNull(navn.fornavn, navn.mellomNavn, navn.etternavn)
                            .joinToString(" ")
                }
            }
        }
    }
}
