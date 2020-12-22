package no.nav.tag.innsynAareg.client.pdl

import com.fasterxml.jackson.core.io.NumberInput
import lombok.RequiredArgsConstructor
import no.nav.tag.innsynAareg.client.pdl.dto.PdlBatchRequest
import no.nav.tag.innsynAareg.client.pdl.dto.PdlBatchRespons
import no.nav.tag.innsynAareg.client.pdl.dto.Variables
import no.nav.tag.innsynAareg.client.sts.STSClient
import no.nav.tag.innsynAareg.utils.GraphQlBatch
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
    val stsClient: STSClient,
    val graphQlUtils: GraphQlBatch,
    @Value("\${pdl.pdlUrl}") pdlUrl: String
) {
    private val uriString: String = pdlUrl

    val logger = org.slf4j.LoggerFactory.getLogger(PdlBatchClient::class.java)!!

    private fun createRequestEntity(pdlRequest: PdlBatchRequest): HttpEntity<Any?> {
        return HttpEntity(pdlRequest, createHeaders())
    }

    private fun createHeaders(): HttpHeaders {
        val stsToken: String? = stsClient.token?.access_token
        val headers = HttpHeaders()

        if (stsToken != null) {
            headers.setBearerAuth(stsToken)
        } else {
            logger.error("fant ikke ststoken i pdlservice")
        }
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Tema"] = "GEN"
        headers["Nav-Consumer-Token"] = "Bearer $stsToken"
        return headers
    }

    fun getBatchFraPdl(fnrs: List<String>): PdlBatchRespons? {
        try {
            val pdlRequest = PdlBatchRequest(
                graphQlUtils.resourceAsString(),
                Variables(fnrs)
            )
            return restTemplate.postForObject(uriString, createRequestEntity(pdlRequest), PdlBatchRespons::class.java)!!
        } catch (exception: Exception) {
            logger.error("AG-ARBEIDSFORHOLD feiler mot PDL ", maskerFødselsnummer(exception.message.toString()))
        }
        return null
    }
}

fun maskerFødselsnummer(beskjed: String): String {
    var filtrertBeskjed = beskjed;
    for (i in 0 until beskjed.length - 10) {
        var erFnr: Boolean;
        val subString = beskjed.substring(i, i+11);
        try {
            NumberInput.parseDouble(subString)
            erFnr=true
        }
        catch (e: Exception) {
            continue
        }
        if (erFnr) {
            filtrertBeskjed = filtrertBeskjed.replace(subString, "***********")
        }
    }
    return filtrertBeskjed;
}