package no.nav.tag.innsynAareg.service.enhetsregisteret

import no.nav.tag.innsynAareg.models.enhetsregisteret.EnhetsRegisterOrg
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class EnhetsregisterService(private val restTemplate: RestTemplate) {
    @Value("\${ereg.url}")
    private val eregUrl: String? = null
    private val requestEntity: HttpEntity<String>
    private fun getRequestEntity(): HttpEntity<String> {
        val headers = HttpHeaders()
        return HttpEntity(headers)
    }

    fun hentOrgnaisasjonFraEnhetsregisteret(orgnr: String): EnhetsRegisterOrg? {
        val eregurMedParam = "$eregUrl$orgnr?inkluderHistorikk=false&inkluderHierarki=true"
        val response: ResponseEntity<EnhetsRegisterOrg> = restTemplate.exchange(eregurMedParam, HttpMethod.GET, requestEntity, EnhetsRegisterOrg::class.java)
        return response.getBody()
    }

    init {
        requestEntity = getRequestEntity()
    }
}