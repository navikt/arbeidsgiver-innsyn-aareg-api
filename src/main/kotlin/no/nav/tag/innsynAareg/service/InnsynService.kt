package no.nav.tag.innsynAareg.service

import no.nav.tag.innsynAareg.client.aareg.AaregClient
import no.nav.tag.innsynAareg.client.aareg.AaregException
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.OrganisasjonFraEreg
import no.nav.tag.innsynAareg.client.tilgangskontroll.TilgangskontrollClient
import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkClient
import no.nav.tag.innsynAareg.models.AltinnOppslagVellykket
import no.nav.tag.innsynAareg.models.ArbeidsforholdFunnet
import no.nav.tag.innsynAareg.models.ArbeidsforholdOppslagResultat
import no.nav.tag.innsynAareg.models.IngenRettigheter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

const val SERVICEKODE_INNSYN_AAREG = "5441"
const val SERVICE_EDITION_INNSYN_AAREG = "1"

@Service
class InnsynService(
    private val aaregClient: AaregClient,
    private val tilgangskontrollClient: TilgangskontrollClient,
    private val yrkeskodeverkClient: YrkeskodeverkClient,
    private val enhetsregisteretService: EnhetsregisteretClient,
    private val navneoppslagService: NavneoppslagService
) {
    private val logger = LoggerFactory.getLogger(InnsynService::class.java)!!

    fun hentAntallArbeidsforholdPåUnderenhet(
        orgnrUnderenhet: String,
        orgnrHovedenhet: String,
    ): Pair<String, Int?> {
        val antall: Int? = aaregClient.antallArbeidsforholdForOpplysningspliktig(
            orgnrUnderenhet,
            orgnrHovedenhet,
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
            )
        } catch (exception: Exception) {
            throw AaregException("Aareg Exception, klarte ikke finne opplysningspliktig: $exception", exception)
        }
    }

    private fun itererOverOrgtre(
        orgnrUnderenhet: String,
        orgledd: OrganisasjonFraEreg,
    ): Pair<String, Int?> {
        val antall = aaregClient.antallArbeidsforholdForOpplysningspliktig(
            orgnrUnderenhet,
            orgledd.organisasjonsnummer,
        )
        if (antall != null && antall != 0) {
            return Pair(orgledd.organisasjonsnummer, antall)
        } else if (orgledd.inngaarIJuridiskEnheter != null) {
            logger.info("traverser opp orgtre")
            try {
                val juridiskEnhetOrgnr: String = orgledd.inngaarIJuridiskEnheter[0].organisasjonsnummer
                val antallNesteNiva = aaregClient.antallArbeidsforholdForOpplysningspliktig(
                    orgnrUnderenhet,
                    juridiskEnhetOrgnr,
                )
                return Pair(juridiskEnhetOrgnr, antallNesteNiva)
            } catch (e: Exception) {
                throw AaregException("Aareg Exception, feilet å finne antall arbeidsforhold på øverste nivå: $e", e)
            }
        } else {
            return itererOverOrgtre(orgnrUnderenhet, orgledd.organisasjonsleddOver!![0].organisasjonsledd)
        }
    }

    fun hentArbeidsforhold(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String,
    ): ArbeidsforholdOppslagResultat {
        val opplysningspliktigorgnr = try {
            hentAntallArbeidsforholdPåUnderenhet(bedriftsnr, overOrdnetEnhetOrgnr).first
        } catch (e: Exception) {
            logger.warn("Exception. Bruker overordnet enhets orgnr fra http-request", e)
            overOrdnetEnhetOrgnr
        }

        return aaregClient.hentArbeidsforhold(bedriftsnr, opplysningspliktigorgnr).apply {
            if (this is ArbeidsforholdFunnet) {
                navneoppslagService.settNavn(oversiktOverArbeidsForhold)
                settYrkeskodebetydningPaAlleArbeidsforhold(oversiktOverArbeidsForhold)
            }
        }
    }

    fun hentTidligereArbeidsforhold(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String,
        fnr: String
    ): ArbeidsforholdOppslagResultat {

        var oversiktOverArbeidsforhold = aaregClient.hentArbeidsforhold(
            bedriftsnr,
            overOrdnetEnhetOrgnr,
        )

        if (oversiktOverArbeidsforhold is IngenRettigheter) {
            oversiktOverArbeidsforhold = finnOpplysningspliktigOgHentArbeidsforhold(
                bedriftsnr,
                overOrdnetEnhetOrgnr,
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
        fnr: String
    ): ArbeidsforholdOppslagResultat {
        val organisasjonerMedTilgang = tilgangskontrollClient.hentOrganisasjonerBasertPaRettigheter()

        if (organisasjonerMedTilgang is AltinnOppslagVellykket) {
            val juridiskeEnhetermedTilgang = organisasjonerMedTilgang
                .organisasjoner
                .filter { it.Type == "Enterprise" }
            juridiskeEnhetermedTilgang.forEach {
                try {
                    val arbeidsforhold = aaregClient.hentArbeidsforhold(
                        bedriftsnr,
                        it.OrganizationNumber!!,
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

