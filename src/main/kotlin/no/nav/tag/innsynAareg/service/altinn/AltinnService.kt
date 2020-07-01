package no.nav.tag.innsynAareg.service.altinn


import lombok.extern.slf4j.Slf4j
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject
import no.nav.security.oidc.context.TokenContext
import no.nav.tag.innsynAareg.models.altinn.AltinnException
import no.nav.tag.innsynAareg.models.altinn.Organisasjon
import no.nav.tag.innsynAareg.service.altinn.AltinnCacheConfig.Companion.ALTINN_TJENESTE_CACHE
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Slf4j
@Component
class AltinnService constructor(altinnConfig: AltinnConfig, private val restTemplate: RestTemplate, tokenUtils: TokenUtils, tokenContext: TokenContext) {
    private val headerEntity: HttpEntity<HttpHeaders?>
    private val tokenUtils: TokenUtils
    private val tokenContext: TokenContext
    private val altinnConfig = altinnConfig;
    private val klient: AltinnrettigheterProxyKlient;

    val logger = LoggerFactory.getLogger(AltinnService::class.java)


    @Cacheable(ALTINN_TJENESTE_CACHE)
    fun hentOrganisasjonerBasertPaRettigheter(fnr: String, serviceKode: String, serviceEdition: String): List<Organisasjon?>? {
            val parametre: MutableMap<String, String> = ConcurrentHashMap()
            parametre["serviceCode"] = serviceKode
            parametre["serviceEdition"] = serviceEdition
            //AltinnService.log.info("Henter rettigheter fra Altinn via proxy")
            try {
                return getReporteesFromAltinnViaProxy(
                    no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject(fnr),
                    parametre
            )}
            catch (error: Exception) {
                logger.error("Klarte ikke hente organisasjoner med rett til arbeidsforhold: ", error.message)
            }
        return null
    }

    fun hentOrganisasjoner(fnr: String): List<Organisasjon?>? {
        val filterParamVerdi = "Type+ne+'Person'+and+Status+eq+'Active'"
            val parametre: MutableMap<String, String> = ConcurrentHashMap()
            parametre["\$filter"] = filterParamVerdi
            try {
                return getReporteesFromAltinnViaProxy(
                        no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject(fnr),
                        parametre
                );

            }
            catch (error: Exception) {
                logger.error("Klarte ikke hente organisasjoner fra Altinn: ", error.message)
            }
        return null;
    }


    private fun getAuthHeadersForInnloggetBruker(): HttpEntity<HttpHeaders?>? {
        val headers = HttpHeaders()
        headers.setBearerAuth(tokenUtils.tokenForInnloggetBruker)
        return HttpEntity(headers)
    }

    fun getReporteesFromAltinnViaProxy(
            subject: Subject?,
            parametre: MutableMap<String, String>
    ): List<Organisasjon?> {
        val response: MutableSet<Organisasjon?> = HashSet()
        var pageNumber = 0
        var hasMore = true
        while (hasMore) {
            pageNumber++
            try {
                parametre["\$top"] = ALTINN_ORG_PAGE_SIZE.toString()
                parametre["\$skip"] = ((pageNumber - 1) * ALTINN_ORG_PAGE_SIZE).toString()
                val collection: MutableList<Organisasjon> = klient.hentOrganisasjoner(tokenContext, subject!!, parametre.toMap()).map { Organisasjon(it.name!!, it.type!!, it.parentOrganizationNumber!!, it.organizationNumber!!, it.organizationForm!!, it.status!!)}.toMutableList();
                response.addAll(collection)
               hasMore = collection.size >= ALTINN_ORG_PAGE_SIZE;
            } catch (exception: RestClientException) {
                //AltinnService.log.error("Feil fra Altinn-proxy med spørring: " + url + " Exception: " + exception.message)
                throw AltinnException("Feil fra Altinn", exception)
            }
        }
        return response.toList()
    }


    companion object {
        private const val ALTINN_ORG_PAGE_SIZE = 500
    }

    init {
        this.tokenUtils = tokenUtils
        this.tokenContext = tokenContext;
        val headers = HttpHeaders()
        headers["APIKEY"] = altinnConfig.altinnHeader
        headerEntity = HttpEntity(headers)
        val proxyKlientConfig = AltinnrettigheterProxyKlientConfig(
                no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig("ditt-nav-arbeidsgiver-api", altinnConfig.proxyUrl),
                no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnConfig(
                        altinnConfig.fallBackUrl,
                        altinnConfig.altinnHeader,
                        altinnConfig.APIGwHeader
                )
        )
        klient = AltinnrettigheterProxyKlient(proxyKlientConfig)
    }
}

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}