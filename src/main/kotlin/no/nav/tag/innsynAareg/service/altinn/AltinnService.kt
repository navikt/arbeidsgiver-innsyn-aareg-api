package no.nav.tag.innsynAareg.service.altinn


import lombok.extern.slf4j.Slf4j
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlientConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.ProxyConfig
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.AltinnReportee
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.model.Subject

import no.nav.tag.innsynAareg.models.altinn.AltinnException
import no.nav.tag.innsynAareg.models.altinn.Organisasjon
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.*

@Slf4j
@Component
class AltinnService constructor(altinnConfig: AltinnConfig, private val restTemplate: RestTemplate, tokenUtils: TokenUtils) {
    private val headerEntity: HttpEntity<HttpHeaders?>
    private val tokenUtils: TokenUtils
    private val altinnConfig = altinnConfig;
    private val klient: AltinnrettigheterProxyKlient;


    fun hentReporteesFraAltinn(altinnQuery: String, fnr: String): List<Organisasjon> {
        var query = altinnQuery
        val baseUrl: String
        baseUrl = altinnConfig.proxyUrl;
        val headers = getAuthHeadersForInnloggetBruker()!!;
        query += "&subject=$fnr"
        val refTilListetype = typeReference<List<Organisasjon>>();
        val url = baseUrl + "reportees/?ForceEIAuthentication" + query
        return getFromAltinn(refTilListetype, url, ALTINN_ORG_PAGE_SIZE, headers)
    }



    fun <T> getFromAltinn(typeReference: ParameterizedTypeReference<List<T>>, url: String, pageSize: Int, headers: HttpEntity<HttpHeaders?>?): List<T> {
        val response = mutableListOf<T>()
        var pageNumber = 0
        var hasMore = true
        while (hasMore) {
            pageNumber++
            try {
                val urlWithPagesizeAndOffset = url + "&\$top=" + pageSize + "&\$skip=" + (pageNumber - 1) * pageSize
                val delRespons  = restTemplate.exchange(urlWithPagesizeAndOffset, HttpMethod.GET, headers, typeReference);
                val delResponsBody = delRespons.body
                if (delResponsBody != null) {
                    response.addAll(delResponsBody)
                    hasMore = (delResponsBody.size >= pageSize)
                };
            } catch (exception: RestClientException) {
                //AltinnService.log.error("Feil fra Altinn med spørring: " + url + " Exception: " + exception.message)
                throw AltinnException("Feil fra Altinn", exception)
            }
        }
        return ArrayList(response)
    }

    private fun getAuthHeadersForInnloggetBruker(): HttpEntity<HttpHeaders?>? {
        val headers = HttpHeaders()
        headers.setBearerAuth(tokenUtils.tokenForInnloggetBruker)
        return HttpEntity(headers)
    }

    /*fun getReporteesFromAltinnViaProxy(
            tokenContext: TokenValidationContextHolder,
            subject: Subject?,
            parametre: MutableMap<String?, String?>,
            url: String,
            pageSize: Int
    ): List<Organisasjon?>? {
        val response: MutableSet<Organisasjon?> = HashSet()
        var pageNumber = 0
        var hasMore = true
        while (hasMore) {
            pageNumber++
            try {
                parametre["\$top"] = pageSize.toString()
                parametre["\$skip"] = ((pageNumber - 1) * pageSize).toString()
                //val collection: List<Organisasjon?> = klient.hentOrganisasjoner.mapTo(klient.hentOrganisasjoner(tokenContext, subject, parametre))
               // response.addAll(collection)
               // hasMore = collection.size >= pageSize
            } catch (exception: RestClientException) {
                //AltinnService.log.error("Feil fra Altinn-proxy med spørring: " + url + " Exception: " + exception.message)
                throw AltinnException("Feil fra Altinn", exception)
            }
        }
        return ArrayList<Any?>(response)
    }

     */

    companion object {
        private const val ALTINN_ORG_PAGE_SIZE = 500
    }

    init {
        this.tokenUtils = tokenUtils
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