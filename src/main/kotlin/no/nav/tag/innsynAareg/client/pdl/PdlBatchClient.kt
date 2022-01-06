package no.nav.tag.innsynAareg.client.pdl

import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class PdlBatchClient @Autowired constructor(
    private val restTemplate: RestTemplate,
    @Value("\${pdl.pdlUrl}") private val pdlUrl: String,
    @Value("\${pdl.tokenClaim}") private val pdlTokenClaim: String,
) {
    private val log = LoggerFactory.getLogger(PdlBatchClient::class.java)!!

    private val azureExchangeClient = AzureServiceBuilder.buildAzureService(
        enableDefaultProxy = true
    )

    fun getBatchFraPdl(fnrs: List<String>): HentPersonBolkResponse? {
        return try {
            getBatchFraPdlInternal(fnrs)
        } catch (exception: Exception) {
            val msg = exception
                .message
                .toString()
                .replace(Regex("""\d{11}"""), "***********")
            log.error("AG-ARBEIDSFORHOLD feiler mot PDL: $msg")
            null
        }
    }

    private fun getBatchFraPdlInternal(fnrs: List<String>): HentPersonBolkResponse {
        val token = runBlocking {
            azureExchangeClient.getAccessToken(pdlTokenClaim)
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Tema"] = "GEN"
        headers.setBearerAuth(token)

        return restTemplate.postForObject(
            pdlUrl,
            HttpEntity(
                HentPersonBolkRequest(fnrs),
                headers
            ),
            HentPersonBolkResponse::class.java
        )!!
    }
}

