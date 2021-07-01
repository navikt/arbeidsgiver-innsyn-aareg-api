package no.nav.tag.innsynAareg.service

import no.nav.tag.innsynAareg.client.aareg.AaregClient
import no.nav.tag.innsynAareg.client.aareg.AaregException
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.OrganisasjonFraEreg
import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkClient
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.client.altinn.AltinnClient
import no.nav.tag.innsynAareg.models.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

const val SERVICEKODE_INNSYN_AAREG = "5441"
const val SERVICE_EDITION_INNSYN_AAREG = "1"

@Service
class InnsynService(
    private val aaregClient: AaregClient,
    private val yrkeskodeverkClient: YrkeskodeverkClient,
    private val enhetsregisteretService: EnhetsregisteretClient,
    private val altinnClient: AltinnClient,
    private val navneoppslagService: NavneoppslagService
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

        val orgtreFraEnhetsregisteret = enhetsregisteretService
            .hentOrganisasjonFraEnhetsregisteret(orgnrUnderenhet, false)

        if (orgtreFraEnhetsregisteret.bestaarAvOrganisasjonsledd.isNullOrEmpty()) {
            return Pair(orgnrHovedenhet, 0)
        }

        return try {
            itererOverOrgtre(
                orgnrUnderenhet,
                orgtreFraEnhetsregisteret.bestaarAvOrganisasjonsledd[0].organisasjonsledd,
                idPortenToken
            )
        } catch (exception: Exception) {
            throw AaregException("Aareg Exception, klarte ikke finne opplysningspliktig: $exception", exception)
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
            } catch (e: Exception) {
                throw AaregException("Aareg Exception, feilet å finne antall arbeidsforhold på øverste nivå: $e", e)
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
        val opplysningspliktigorgnr = try {
            hentAntallArbeidsforholdPåUnderenhet(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken).first
        } catch (e: Exception) {
            logger.warn("Exception. Bruker overordnet enhets orgnr fra http-request", e)
            overOrdnetEnhetOrgnr
        }

        val arbeidsforhold = aaregClient.hentArbeidsforhold(bedriftsnr, opplysningspliktigorgnr, idPortenToken)

        if (arbeidsforhold is ArbeidsforholdFunnet) {
            navneoppslagService.settNavn(arbeidsforhold.oversiktOverArbeidsForhold)
            settYrkeskodebetydningPaAlleArbeidsforhold(arbeidsforhold.oversiktOverArbeidsForhold)
        }

        return arbeidsforhold
    }


    fun hentTidligereArbeidsforhold(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String,
        idPortenToken: String,
        fnr: String
    ): ArbeidsforholdOppslagResultat {

        var oversiktOverArbeidsforhold = aaregClient.hentArbeidsforhold(
            bedriftsnr,
            overOrdnetEnhetOrgnr,
            idPortenToken
        )

        if (oversiktOverArbeidsforhold !is ArbeidsforholdFunnet) {
            oversiktOverArbeidsforhold = finnOpplysningspliktigOgHentArbeidsforhold(
                bedriftsnr,
                overOrdnetEnhetOrgnr,
                idPortenToken,
                fnr
            )
        }

        if (oversiktOverArbeidsforhold is ArbeidsforholdFunnet) {
            navneoppslagService.settNavn(oversiktOverArbeidsforhold.oversiktOverArbeidsForhold)
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
        val organisasjonerMedTilgang = altinnClient.hentOrganisasjonerBasertPaRettigheter(
            fnr,
            SERVICEKODE_INNSYN_AAREG,
            SERVICE_EDITION_INNSYN_AAREG
        )

        if (organisasjonerMedTilgang is AltinnOppslagVellykket) {
            val juridiskeEnhetermedTilgang = organisasjonerMedTilgang
                .organisasjoner
                .filter { it.Type == "Enterprise" }
            juridiskeEnhetermedTilgang.forEach {
                try {
                    val arbeidsforhold = aaregClient.hentArbeidsforhold(
                        bedriftsnr,
                        it.OrganizationNumber!!,
                        idPortenToken
                    )
                    if (arbeidsforhold is ArbeidsforholdFunnet) {
                        return arbeidsforhold
                    }
                } catch (e: Exception) {
                    logger.error("klarte ikke hente historiske arbeidsforhold $e", e)
                }
            }
        }

        return IngenRettigheter
    }

    private fun settYrkeskodebetydningPaAlleArbeidsforhold(
        arbeidsforholdOversikt: OversiktOverArbeidsForhold
    ) {
        val yrkeskodeBeskrivelser = yrkeskodeverkClient.hentBetydningAvYrkeskoder()
        for (arbeidsforhold in arbeidsforholdOversikt.arbeidsforholdoversikter!!) {
            arbeidsforhold.yrkesbeskrivelse = yrkeskodeBeskrivelser.betydningPåYrke(arbeidsforhold.yrke)
        }
    }
}

