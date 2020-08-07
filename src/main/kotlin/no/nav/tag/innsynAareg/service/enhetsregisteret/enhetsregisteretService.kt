package no.nav.tag.innsynAareg.service.enhetsregisteret

import lombok.extern.slf4j.Slf4j
import no.nav.tag.innsynAareg.models.enhetsregisteret.EnhetsRegisterOrg
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.lang.Exception

@Slf4j
@Service
class EnhetsregisterService(private val restTemplate: RestTemplate) {
    val logger = LoggerFactory.getLogger(EnhetsregisterService::class.java)
    @Value("\${ereg.url}")
    private val eregUrl: String? = null
    private val requestEntity: HttpEntity<String>
    private fun getRequestEntity(): HttpEntity<String> {
        val headers = HttpHeaders()
        return HttpEntity(headers)
    }

    fun hentOrgnaisasjonFraEnhetsregisteret(orgnr: String): EnhetsRegisterOrg? {
        try {
            val eregurMedParam = "$eregUrl$orgnr?inkluderHistorikk=false&inkluderHierarki=true"
            val response: ResponseEntity<EnhetsRegisterOrg> = restTemplate.exchange(eregurMedParam, HttpMethod.GET, requestEntity, EnhetsRegisterOrg::class.java)
            logger.info("respons fra enhetsregisteret: ", response.body)
            logger.info("sjekker om organisasjon inngår i orgledd for organisasjon: $orgnr")
            return response.body
        }
        catch (e: Exception) {
            logger.error("Feil ved oppslag mot EnhetsRegisteret: orgnr: $orgnr", e.message);
        }
        return null;
    }

    init {
        requestEntity = getRequestEntity()
    }
}