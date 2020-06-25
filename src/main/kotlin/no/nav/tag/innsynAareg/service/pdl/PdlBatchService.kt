package no.nav.tag.innsynAareg.service.pdl;

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

@Slf4j
@Service
@RequiredArgsConstructor
class PdlBatchService @Autowired constructor(private val restTemplate: RestTemplate, val stsClient: STSClient, val graphQlUtils: GraphQlUtils, @Value("\${pdl.pdlUrl}") pdlUrl: String) {
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

    fun getBatchFraPdl(fnrs: Array<String?>?): PdlBatchRespons? {
        try {
            val pdlRequest = PdlBatchRequest(graphQlUtilsBatch.resourceAsString(), Variables(fnrs))
            val entity: HttpEntity<*> = createRequestEntityBatchSporring(pdlRequest)
            PdlService.log.info("MSA-AAREG-PDL: PDLBATCHREQUEST: " + createRequestEntityBatchSporring(pdlRequest))
            PdlService.log.info("MSA-AAREG-PDL: requestEntity i batch $entity")
            //return  restTemplate.postForObject(pdlUrl, createRequestEntityBatchSporring(), PdlBatchRespons.class);
            return restTemplate.postForObject(pdlUrl, createRequestEntityBatchSporring(pdlRequest), PdlBatchRespons::class.java)
        } catch (exception: RestClientException) {
            PdlService.log.error("MSA-AAREG-PDL: Exception: {} i PDLBATCH" + exception.message)
        } catch (exception: IOException) {
            PdlService.log.error("MSA-AAREG-PDL: Exception: {} i PDLBATCH" + exception.message)
        }
        return null
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

    private fun createRequestEntity(pdlRequest: PdlRequest): HttpEntity<Any?> {
        return HttpEntity(pdlRequest, createHeaders())
    }

    private fun lagManglerNavnException(): Navn {
        logger.error("lag mangler exception navn ");
        val exceptionNavn = Navn("Kunne ikke hente navn",null,null)
        logger.error("lag mangler exception navn {}",exceptionNavn);
        return exceptionNavn
    }

    private fun lesNavnFraPdlRespons(respons: PdlRespons): Navn {
        return try {
            if(respons.data!!.hentPerson!!.navn!!.size>0) {
                respons.data.hentPerson!!.navn!!.first()
            }else{
                lagManglerNavnException()
            }
        } catch (e: KotlinNullPointerException) {
            logger.error("PDL exception: respons {} ", respons);
            logger.error("PDL exception: {} ", e.message)
            logger.error("PDL exception: {} ", e.cause);
            if ( respons.errors.isNullOrEmpty() ) {
                logger.error("AAREG pdlerror: " + respons.errors?.first().toString())
            }
            else {
                logger.error("AAREG nullpointer: helt tom respons fra pdl")
            }
            lagManglerNavnException()
        }
    }

    suspend fun getFraPdl(fnr: String): Navn? {
    return try {
        val variables = Variables(fnr);
        logger.info("AAREG arbeidsforhold variables ident {}", variables.ident);
        val pdlRequest = PdlRequest(graphQlUtils.resourceAsString(), variables)
        logger.info("pdl request query: {}", pdlRequest.query)
        logger.info("pdl request variable: {}", pdlRequest.variables)
        val respons: PdlRespons? = restTemplate.postForObject(uriString, createRequestEntity(pdlRequest), PdlRespons::class.java)
        if(respons!=null){
            logger.info("pdl respons: {}", respons)
            lesNavnFraPdlRespons(respons)}
        else{
            logger.error("tom pdl respons ")
            lagManglerNavnException()
        }

    } catch (exception: RestClientException) {
        logger.error("MSA-AAREG Exception: {}", exception.message)
        lagManglerNavnException()
    } catch (exception: IOException) {
        logger.error("MSA-AAREG Exception: {}", exception.message)
        lagManglerNavnException()
    }
    }

}
