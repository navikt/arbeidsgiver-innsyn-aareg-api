package no.nav.tag.innsynAareg.client.pdl

import no.nav.tag.innsynAareg.client.azure.AzureClient
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
    private val azureClient: AzureClient,
    @Value("\${pdl.pdlUrl}") private val pdlUrl: String,
    @Value("\${pdl.pdlScope}") private val pdlScope: String
) {
    private val log = LoggerFactory.getLogger(PdlBatchClient::class.java)!!

    fun getBatchFraPdl(fnrs: List<String>): HentPersonBolkResponse? {
        return try {
            getBatchFraPdlInternal(fnrs)
        } catch (exception: Exception) {
            val msg = exception
                .message
                .toString()
                .replace(Regex("""\d{11}"""), "***********")
            log.error("AG-ARBEIDSFORHOLD feiler mot PDL: $msg , pdlUrl: $pdlUrl, pdlScope: $pdlScope")
            null
        }
    }

    private fun getBatchFraPdlInternal(fnrs: List<String>): HentPersonBolkResponse {
        val azureToken: String = azureClient.getToken(pdlScope)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Tema"] = "GEN"
        headers["Behandlingsnummer"] = "B415"
        headers.setBearerAuth(azureToken)

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

