package no.nav.tag.innsynAareg.client.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.*
import no.nav.tag.innsynAareg.client.altinn.AltinnCacheConfig.Companion.ALTINN_TJENESTE_CACHE
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.models.AltinnIngenRettigheter
import no.nav.tag.innsynAareg.models.AltinnOppslagResultat
import no.nav.tag.innsynAareg.models.AltinnOppslagVellykket
import no.nav.tag.innsynAareg.utils.AutentisertBruker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class AltinnClient(
    @Value("\${altinn.proxyUrl}") val proxyUrl: String,
    @Value("\${altinn.altinnUrl}") val fallBackUrl: String,
    @Value("\${altinn.altinnHeader}") val altinnHeader: String,
    @Value("\${altinn.APIGwHeader}") val APIGwHeader: String,
    private val autentisertBruker: AutentisertBruker,
) {
    val logger = LoggerFactory.getLogger(AltinnClient::class.java)!!

    private val klient = AltinnrettigheterProxyKlient(
        AltinnrettigheterProxyKlientConfig(
            ProxyConfig("arbeidsgiver-arbeidsforhold-api", proxyUrl),
            AltinnConfig(fallBackUrl, altinnHeader, APIGwHeader)
        )
    )

    @Cacheable(ALTINN_TJENESTE_CACHE)
    fun hentOrganisasjonerBasertPaRettigheter(
        fnr: String,
        serviceKode: String,
        serviceEdition: String
    ): AltinnOppslagResultat =
        run {
            klient.hentOrganisasjoner(
                SelvbetjeningToken(autentisertBruker.jwtToken),
                Subject(fnr),
                ServiceCode(serviceKode),
                ServiceEdition(serviceEdition),
                false
            )
        }

    fun hentOrganisasjoner(fnr: String): AltinnOppslagResultat =
        run {
            klient.hentOrganisasjoner(
                SelvbetjeningToken(autentisertBruker.jwtToken),
                Subject(fnr),
                true
            )
        }

    private fun run(action: () -> List<AltinnReportee>) =
        try {
            action()
                .map {
                    Organisasjon(
                        Name = it.name,
                        ParentOrganizationNumber = it.parentOrganizationNumber,
                        OrganizationNumber = it.organizationNumber,
                        OrganizationForm = it.organizationForm,
                        Status = it.status,
                        Type = it.type
                    )
                }
                .let { AltinnOppslagVellykket(it) }
        } catch (error: Exception) {
            if (error.message?.contains("403") == true)
                AltinnIngenRettigheter
            else
                throw RuntimeException("AG-ARBEIDSFORHOLD Klarte ikke hente organisasjoner fra altinn. $error", error)
        }
}