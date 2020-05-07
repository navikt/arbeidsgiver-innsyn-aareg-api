package no.nav.tag.innsynAareg.service.pdl

import kotlinx.coroutines.delay
import lombok.RequiredArgsConstructor
import lombok.SneakyThrows
import lombok.extern.slf4j.Slf4j
import no.nav.tag.innsynAareg.models.pdlPerson.Navn
import no.nav.tag.innsynAareg.models.pdlPerson.PdlRequest
import no.nav.tag.innsynAareg.models.pdlPerson.PdlRespons
import no.nav.tag.innsynAareg.models.pdlPerson.Variables
import no.nav.tag.innsynAareg.service.sts.STSClient
import no.nav.tag.innsynAareg.utils.GraphQlUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.lang.NullPointerException

@Slf4j
@Service
@RequiredArgsConstructor
class PdlService @Autowired constructor(private val restTemplate: RestTemplate, val stsClient: STSClient, val graphQlUtils: GraphQlUtils, @Value("\${pdl.pdlUrl}") pdlUrl: String) {
    private val uriString: String = pdlUrl;

    val logger = org.slf4j.LoggerFactory.getLogger(PdlService::class.java)

    @SneakyThrows
    suspend fun hentNavnMedFnr(fnr: String): String {
        val result: Navn? = getFraPdl(fnr)
        var navn = ""
        if (result?.fornavn != null) navn += result.fornavn
        if (result?.mellomNavn != null) navn += " " + result.mellomNavn
        if (result?.etternavn != null) navn += " " + result.etternavn
        return navn
    }

    private fun createHeaders(): HttpHeaders {
        val stsToken: String? = stsClient?.token?.access_token;
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers["Authorization"] = "Bearer $stsToken"
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Tema"] = "GEN"
        headers["Nav-Consumer-Token"] = "Bearer $stsToken"
        return headers
    }

    private fun createRequestEntity(pdlRequest: PdlRequest): HttpEntity<Any?> {
        return HttpEntity<Any?>(pdlRequest, createHeaders())
    }

    private fun lagManglerNavnException(): Navn {
        val exceptionNavn = Navn()
        exceptionNavn.fornavn = "Kunne ikke hente navn"
        return exceptionNavn
    }

    private fun lesNavnFraPdlRespons(respons: PdlRespons?): Navn? {
        try {
            return respons?.data?.hentPerson?.navn!!.first()
        } catch (e: Exception) {
            logger.error("AAREG exception: {} ", e.message)
            if ( !respons?.errors.isNullOrEmpty() ) {
                logger.error("AAREG pdlerror: " + respons?.errors?.first().toString())
            }
            else {
                logger.error("AAREG nullpointer: helt tom respons fra pdl")
            }
        }
        return lagManglerNavnException()
    }

    suspend fun getFraPdl(fnr: String): Navn? {
        return try {
            val variables = Variables(fnr);
            logger.error("AAREG arbeidsforhold variables er", variables);
            val pdlRequest = PdlRequest(graphQlUtils.resourceAsString(), variables)
           val respons: PdlRespons? = restTemplate.postForObject(uriString, createRequestEntity(pdlRequest), PdlRespons::class.java)
            lesNavnFraPdlRespons(respons)
        } catch (exception: RestClientException) {
            logger.error("MSA-AAREG Exception: {}", exception.message)
            lagManglerNavnException()
        } catch (exception: IOException) {
            logger.error("MSA-AAREG Exception: {}", exception.message)
            lagManglerNavnException()
        }
    }

}