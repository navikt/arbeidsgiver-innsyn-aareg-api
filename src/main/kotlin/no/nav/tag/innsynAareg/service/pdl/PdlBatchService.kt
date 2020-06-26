package no.nav.tag.innsynAareg.service.pdl;

import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import no.nav.tag.innsynAareg.models.pdlBatch.PdlBatchRequest
import no.nav.tag.innsynAareg.models.pdlBatch.PdlBatchRespons
import no.nav.tag.innsynAareg.models.pdlBatch.Variables
import no.nav.tag.innsynAareg.models.pdlPerson.Navn
import no.nav.tag.innsynAareg.service.sts.STSClient
import no.nav.tag.innsynAareg.utils.GraphQlBatch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.lang.Exception

@Slf4j
@Service
@RequiredArgsConstructor
class PdlBatchService @Autowired constructor(private val restTemplate: RestTemplate, val stsClient: STSClient, val graphQlUtils: GraphQlBatch, @Value("\${pdl.pdlUrl}") pdlUrl: String) {
    private val uriString: String = pdlUrl;

    val logger = org.slf4j.LoggerFactory.getLogger(PdlService::class.java)

    private fun createRequestEntity(pdlRequest: PdlBatchRequest): HttpEntity<Any?> {
        return HttpEntity(pdlRequest, createHeaders())
    }

    private fun createHeaders(): HttpHeaders {
        val stsToken: String? = stsClient.token?.access_token;
        val headers = HttpHeaders()

        if (stsToken != null) {
            headers.setBearerAuth(stsToken)
        }else{
            logger.error("fant ikke ststoken i pdlservice")
        }
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Tema"] = "GEN"
        headers["Nav-Consumer-Token"] = "Bearer $stsToken"
        return headers
    }


    fun getBatchFraPdl(fnrs: Array<String>?): PdlBatchRespons? {
        try {
            val pdlRequest = PdlBatchRequest(graphQlUtils.resourceAsString(), Variables(fnrs))
            logger.info("AG-ARBEIDSFORHOLD PDL henter navn");
            return restTemplate.postForObject(uriString, createRequestEntity(pdlRequest), PdlBatchRespons::class.java)!!
        } catch (exception: Exception) {
            logger.error("AG-ARBEIDSFORHOLD feiler mot PDL ", exception.message );
        }
        return null
    }

}
