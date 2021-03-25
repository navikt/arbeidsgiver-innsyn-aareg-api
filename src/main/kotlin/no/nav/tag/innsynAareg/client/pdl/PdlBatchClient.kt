package no.nav.tag.innsynAareg.client.pdl

import lombok.RequiredArgsConstructor
import no.nav.tag.innsynAareg.client.pdl.dto.PdlBatchRequest
import no.nav.tag.innsynAareg.client.pdl.dto.PdlBatchRespons
import no.nav.tag.innsynAareg.client.pdl.dto.Variables
import no.nav.tag.innsynAareg.client.sts.STSClient
import no.nav.tag.innsynAareg.utils.GraphQlBatch
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@RequiredArgsConstructor
class PdlBatchClient @Autowired constructor(
    private val restTemplate: RestTemplate,
    private val stsClient: STSClient,
    private val graphQlUtils: GraphQlBatch,
    @Value("\${pdl.pdlUrl}") private val pdlUrl: String
) {
    private val log = LoggerFactory.getLogger(PdlBatchClient::class.java)!!

    fun getBatchFraPdl(fnrs: List<String>): PdlBatchRespons? {
        return try {
            getBatchFraPdlInternal(fnrs)
        } catch (exception: Exception) {
            val msg = exception
                .message
                .toString()
                .replace(Regex("""\d{11}"""), "***********")
            log.error("AG-ARBEIDSFORHOLD feiler mot PDL ", msg)
            null
        }
    }

    private fun getBatchFraPdlInternal(fnrs: List<String>): PdlBatchRespons {
        val stsToken: String? = stsClient.token?.access_token
        val headers = HttpHeaders()

        if (stsToken != null) {
            headers.setBearerAuth(stsToken)
        } else {
            log.error("fant ikke ststoken i pdlservice")
        }
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Tema"] = "GEN"
        headers["Nav-Consumer-Token"] = "Bearer $stsToken"

        val pdlRequest = PdlBatchRequest(
            graphQlUtils.resourceAsString(),
            Variables(fnrs)
        )
        return restTemplate.postForObject(
            pdlUrl,
            HttpEntity(pdlRequest, headers),
            PdlBatchRespons::class.java
        )!!
    }
}

