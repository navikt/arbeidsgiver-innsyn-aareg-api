package no.nav.tag.innsynAareg.services.sts

import lombok.extern.slf4j.Slf4j
import no.nav.tag.innsynAareg.InnsynAaregApplication
import no.nav.tag.innsynAareg.services.sts.STSCacheConfig.Companion.STS_CACHE

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Slf4j
@Component
class STSClient @Autowired
constructor(@Value("sts.stsPass") stsPass: String,
            @Value("sts.stsUrl") stsUrl: String,
            private val restTemplate: RestTemplate ) {
    private val requestEntity: HttpEntity<String>
    private val uriString: String

    val token: STStoken?
        @Cacheable(STS_CACHE)
        get() {
            try {
                val response = restTemplate.exchange(uriString, HttpMethod.GET, requestEntity, STStoken::class.java)
                if (response.getStatusCode() != HttpStatus.OK) {
                    val message = "Kall mot STS feiler med HTTP-" + response.getStatusCode()
                    //log.error(message)
                    throw RuntimeException(message)
                }
                return response.getBody()
            } catch (e: HttpClientErrorException) {
                //log.error("Feil ved oppslag i STS", e)
                throw RuntimeException(e)
            }

        }

    init {
        this.requestEntity = getRequestEntity(stsPass)
        this.uriString = buildUriString(stsUrl);
    }

    private fun getRequestEntity(stsPass: String): HttpEntity<String> {
        val headers = HttpHeaders()
        headers.setBasicAuth(InnsynAaregApplication.APP_NAME, stsPass)
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        return HttpEntity(headers)
    }

    private fun buildUriString(stsUrl: String): String {
        return UriComponentsBuilder.fromHttpUrl(stsUrl)
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "openid")
                .toUriString()
    }

    @CacheEvict(STS_CACHE)
    fun evict() {
    }

}
