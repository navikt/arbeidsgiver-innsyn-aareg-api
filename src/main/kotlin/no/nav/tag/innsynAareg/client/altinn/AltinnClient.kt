package no.nav.tag.innsynAareg.client.altinn

import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.SelvbetjeningToken
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject
import no.nav.tag.innsynAareg.client.altinn.AltinnCacheConfig.Companion.ALTINN_TJENESTE_CACHE
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class AltinnClient constructor(
    @Value("\${altinn.proxyUrl}") val proxyUrl: String,
    tokenUtils: TokenUtils,
    @Value("\${altinn.altinnUrl") val fallBackUrl: String,
    @Value("\${altinn.altinnHeader}") val altinnHeader: String,
    @Value("\${altinn.APIGwHeader}") val APIGwHeader: String
) {
    private val tokenUtils: TokenUtils = tokenUtils
    private val klient: AltinnrettigheterProxyKlient

    val logger = LoggerFactory.getLogger(AltinnClient::class.java)!!

    @Cacheable(ALTINN_TJENESTE_CACHE)
    fun hentOrganisasjonerBasertPaRettigheter(
        fnr: String,
        serviceKode: String,
        serviceEdition: String
    ): List<Organisasjon>? {
        val parametre: MutableMap<String, String> = ConcurrentHashMap()
        parametre["serviceCode"] = serviceKode
        parametre["serviceEdition"] = serviceEdition

        try {
            return getReporteesFromAltinnViaProxy(
                fnr,
                parametre
            )
        } catch (error: Exception) {
            logger.error("AG-ARBEIDSFORHOLD Klarte ikke hente organisasjoner med rett til arbeidsforhold: ", error.message)
        }
        return null
    }

    fun hentOrganisasjoner(fnr: String): List<Organisasjon>? {
        val filterParamVerdi = "Type+ne+'Person'+and+Status+eq+'Active'"
        val parametre: MutableMap<String, String> = ConcurrentHashMap()
        parametre["\$filter"] = filterParamVerdi
        try {
            return getReporteesFromAltinnViaProxy(
                fnr,
                parametre
            )
        } catch (error: Exception) {
            logger.error("AG-ARBEIDSFORHOLD Klarte ikke hente organisasjoner fra Altinn: ", error.message)
        }
        return null
    }

    fun getReporteesFromAltinnViaProxy(
        fnr: String,
        parametre: MutableMap<String, String>
    ): List<Organisasjon> {
        val response: MutableSet<Organisasjon> = HashSet()
        var pageNumber = 0
        var hasMore = true
        while (hasMore) {
            pageNumber++
            try {
                parametre["\$top"] = ALTINN_ORG_PAGE_SIZE.toString()
                parametre["\$skip"] = ((pageNumber - 1) * ALTINN_ORG_PAGE_SIZE).toString()
                val collectionRAW: List<AltinnReportee> = klient.hentOrganisasjoner(
                    SelvbetjeningToken(tokenUtils.tokenForInnloggetBruker),
                    Subject(fnr),
                    parametre
                )
                val collection: List<Organisasjon> = mapTilOrganisasjon(collectionRAW)
                response.addAll(collection)
                hasMore = collection.size >= ALTINN_ORG_PAGE_SIZE
            } catch (exception: RestClientException) {
                throw AltinnException(
                    "AG-ARBEIDSFORHOLD Feil fra Altinn",
                    exception
                )
            }
        }
        return response.toList()
    }

    companion object {
        private const val ALTINN_ORG_PAGE_SIZE = 500
    }

    init {
        val proxyKlientConfig = AltinnrettigheterProxyKlientConfig(
            ProxyConfig("arbeidsgiver-arbeidsforhold-api", proxyUrl),
            AltinnConfig(fallBackUrl, altinnHeader, APIGwHeader)
        )
        klient = AltinnrettigheterProxyKlient(proxyKlientConfig)
    }

    fun mapTilOrganisasjon(originalListe: List<AltinnReportee>): List<Organisasjon> =
        originalListe.map {
            Organisasjon(
                Name = it.name,
                Status = it.status,
                Type = it.type,
                ParentOrganizationNumber = it.parentOrganizationNumber,
                OrganizationForm = it.organizationForm,
                OrganizationNumber = it.organizationNumber
            )
        }
}