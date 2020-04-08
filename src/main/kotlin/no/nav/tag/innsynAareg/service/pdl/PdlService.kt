package no.nav.tag.innsynAareg.service.pdl

import lombok.RequiredArgsConstructor
import lombok.SneakyThrows
import lombok.extern.slf4j.Slf4j
import no.nav.tag.innsynAareg.models.pdlPerson.Navn
import no.nav.tag.innsynAareg.models.pdlPerson.PdlRequest
import no.nav.tag.innsynAareg.models.pdlPerson.PdlRespons
import no.nav.tag.innsynAareg.service.sts.STSClient
import no.nav.tag.innsynAareg.utils.GraphQlUtils
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
class PdlService {
    private val graphQlUtils: GraphQlUtils? = null
    private val stsClient: STSClient? = null
    private val restTemplate: RestTemplate? = null
    @Value("\${pdl.pdlUrl}")
    var pdlUrl: String? = null

    @SneakyThrows
    fun hentNavnMedFnr(fnr: String): String {
        val result: Navn = getFraPdl(fnr)
        var navn = ""
        if (result.fornavn != null) navn += result.fornavn
        if (result.mellomNavn != null) navn += " " + result.mellomNavn
        if (result.etternavn != null) navn += " " + result.etternavn
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

    private fun lesNavnFraPdlRespons(respons: PdlRespons?): Navn {
        try {
            return respons.data.hentPerson.navn.get(0)
        } catch (e: NullPointerException) {
            no.nav.tag.dittNavArbeidsgiver.services.pdl.PdlService.log.error("MSA-AAREG nullpointer exception: {} ", e.message)
            if (respons.errors != null && !respons.errors.isEmpty()) {
                no.nav.tag.dittNavArbeidsgiver.services.pdl.PdlService.log.error("MSA-AAREG pdlerror: " + respons.errors.get(0).message)
            } else {
                no.nav.tag.dittNavArbeidsgiver.services.pdl.PdlService.log.error("MSA-AAREG nullpointer: helt tom respons fra pdl")
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            no.nav.tag.dittNavArbeidsgiver.services.pdl.PdlService.log.error("MSA-AAREG nullpointer exception: {} ", e.message)
            if (respons.errors != null && !respons.errors.isEmpty()) {
                no.nav.tag.dittNavArbeidsgiver.services.pdl.PdlService.log.error("MSA-AAREG pdlerror: " + respons.errors.get(0).message)
            } else {
                no.nav.tag.dittNavArbeidsgiver.services.pdl.PdlService.log.error("MSA-AAREG nullpointer: helt tom respons fra pdl")
            }
        }
        return lagManglerNavnException()
    }

    private fun getFraPdl(fnr: String): Navn {
        return try {
            //val pdlRequest = PdlRequest(graphQlUtils.resourceAsString(), Variables(fnr))
           // val respons: PdlRespons = restTemplate!!.postForObject(pdlUrl!!, createRequestEntity(pdlRequest), PdlRespons::class.java)
            lesNavnFraPdlRespons(respons)
        } catch (exception: RestClientException) {
            no.nav.tag.dittNavArbeidsgiver.services.pdl.PdlService.log.error("MSA-AAREG Exception: {}", exception.message)
            lagManglerNavnException()
        } catch (exception: IOException) {
            no.nav.tag.dittNavArbeidsgiver.services.pdl.PdlService.log.error("MSA-AAREG Exception: {}", exception.message)
            lagManglerNavnException()
        }
    }
}