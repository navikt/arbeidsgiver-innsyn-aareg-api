package no.nav.tag.innsynAareg.service

import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.service.sts.STSClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class AaregService (val restTemplate: RestTemplate, val stsClient: STSClient){
    @Value("\${aareg.aaregArbeidsforhold}")
    lateinit var aaregArbeidsforholdUrl: String

    fun hentArbeidsforhold(bedriftsnr:String, overOrdnetEnhetOrgnr:String,idPortenToken: String):OversiktOverArbeidsForhold {
        val url = aaregArbeidsforholdUrl
        val entity: HttpEntity<String> = getRequestEntity(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken)
        return try {
            val respons = restTemplate.exchange(url,
                    HttpMethod.GET, entity, OversiktOverArbeidsForhold::class.java)
            if (respons.statusCode != HttpStatus.OK) {
                val message = "Kall mot aareg feiler med HTTP-" + respons.statusCode
                throw RuntimeException(message)
            }
            respons.body!!
        } catch (exception: RestClientException) {
            throw RuntimeException(" Aareg Exception: $exception")
        }
    }

    private fun getRequestEntity(bedriftsnr: String, juridiskEnhetOrgnr: String, idPortenToken: String): HttpEntity<String> {
        val appName = "srvditt-nav-arbeid"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers["Authorization"] = "Bearer $idPortenToken"
        headers["Nav-Call-Id"] = appName
        headers["Nav-Arbeidsgiverident"] = bedriftsnr
        headers["Nav-Opplysningspliktigident"] = juridiskEnhetOrgnr
        headers["Nav-Consumer-Token"] = stsClient.token?.access_token;
        return HttpEntity(headers)
    }


}