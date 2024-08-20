package no.nav.tag.innsynAareg.client.yrkeskoder

import no.nav.tag.innsynAareg.client.azure.AzureClient
import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkCacheConfig.Companion.YRKESKODE_CACHE
import no.nav.tag.innsynAareg.client.yrkeskoder.dto.Yrkeskoderespons
import no.nav.tag.innsynAareg.models.Yrkeskoder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class YrkeskodeverkClient @Autowired constructor(
    private val restTemplate: RestTemplate,
    @Value("\${yrkeskodeverk.yrkeskodeUrl}") val yrkeskodeUrl: String,
    private val azureClient: AzureClient,
    @Value("\${yrkeskodeverk.yrkeskodeScope}") private val yrkeskodeScope: String
) {
    private val logger = LoggerFactory.getLogger(YrkeskodeverkClient::class.java)!!

    @Cacheable(YRKESKODE_CACHE)
    fun hentBetydningAvYrkeskoder(): Yrkeskoder =
        hentYrkeskoderespons()
            .betydninger
            .mapValues { it.value.getOrNull(0)?.beskrivelser?.nb?.tekst ?: "Fant ikke yrkesbeskrivelse" }
            .let(::Yrkeskoder)

    private fun hentYrkeskoderespons(): Yrkeskoderespons {
        return try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            headers["Nav-Call-Id"] = UUID.randomUUID().toString()
            headers["Nav-Consumer-Id"] = "srvAG-Arbforhold"
            headers.setBearerAuth(azureClient.getToken(yrkeskodeScope))
            restTemplate.exchange(
                yrkeskodeUrl,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                Yrkeskoderespons::class.java
            ).body!!
        } catch (e: Exception) {
            logger.error("MSA-AAREG Kall mot kodeoversikt feilet med exception $e", e)
            Yrkeskoderespons()
        }
    }
}