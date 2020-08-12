package no.nav.tag.innsynAareg.client.enhetsregisteret

import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.EnhetsRegisterOrg
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class EnhetsregisteretClient(private val restTemplate: RestTemplate) {
    val logger = LoggerFactory.getLogger(EnhetsregisteretClient::class.java)!!

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
            val response: ResponseEntity<EnhetsRegisterOrg> = restTemplate.exchange(
                eregurMedParam,
                HttpMethod.GET,
                requestEntity,
                EnhetsRegisterOrg::class.java
            )
            logger.info("respons fra enhetsregisteret: ", response.body)
            logger.info("sjekker om organisasjon inngår i orgledd for organisasjon: $orgnr")
            return response.body
        } catch (e: Exception) {
            logger.error("Feil ved oppslag mot EnhetsRegisteret: orgnr: $orgnr", e.message)
        }
        return null
    }

    init {
        requestEntity = getRequestEntity()
    }
}