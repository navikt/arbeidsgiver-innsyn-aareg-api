package no.nav.tag.innsynAareg.client.yrkeskoder


import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkCacheConfig.Companion.YRKESKODE_CACHE
import no.nav.tag.innsynAareg.models.yrkeskoder.Yrkeskoderespons
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class YrkeskodeverkClient @Autowired constructor(
    private val restTemplate: RestTemplate,
    @Value("\${yrkeskodeverk.yrkeskodeUrl}") yrkeskodeUrl: String
) {
    private val headerEntity: HttpEntity<String>
    private val uriString: String = yrkeskodeUrl

    val logger = LoggerFactory.getLogger(YrkeskodeverkClient::class.java)!!


    @Cacheable(YRKESKODE_CACHE)
    fun hentBetydningerAvYrkeskoder(): Yrkeskoderespons? {
        return try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            headers["Nav-Call-Id"] = UUID.randomUUID().toString()
            headers["Nav-Consumer-Id"] = "srvAG-Arbforhold"
            val respons: ResponseEntity<Yrkeskoderespons> = restTemplate.exchange(
                uriString,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                Yrkeskoderespons::class.java
            )
            if (respons.getStatusCode() != HttpStatus.OK) {
                val message = "MSA-AAREG Kall mot kodeverksoversikt feiler med HTTP-" + respons.getStatusCode()
                //no.nav.tag.innsynAareg.service.yrkeskoder.KodeverkService.log.error(message)
                throw RuntimeException(message)
            }
            if (respons.body!!.betydninger.isEmpty()) {
                logger.error("ingen betyrninger funnet i yrkesoppslag")
            }
            respons.getBody()
        } catch (e: HttpClientErrorException) {
            throw RuntimeException(e)
        }
    }

    init {
        val headers = HttpHeaders()
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        headers["Nav-Consumer-Id"] = "srvAG-Arbforhold"
        headerEntity = HttpEntity(headers)
    }
}