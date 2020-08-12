package no.nav.tag.innsynAareg.client.aareg

import no.nav.tag.innsynAareg.client.sts.STSClient
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.models.OversiktOverArbeidsgiver
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Service
class AaregClient(
    val restTemplate: RestTemplate,
    val stsClient: STSClient
) {
    @Value("\${aareg.aaregArbeidsforhold}")
    lateinit var aaregArbeidsforholdUrl: String

    @Value("\${aareg.aaregArbeidsgivere}")
    lateinit var aaregArbeidsgiverOversiktUrl: String

    val logger = LoggerFactory.getLogger(AaregClient::class.java)!!

    fun hentArbeidsforholdFraAAReg(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String,
        idPortenToken: String
    ): OversiktOverArbeidsForhold {
        val url = aaregArbeidsforholdUrl
        val entity: HttpEntity<String> = getRequestEntity(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken)
        return try {
            val respons = restTemplate.exchange(
                url,
                HttpMethod.GET, entity, OversiktOverArbeidsForhold::class.java
            )
            if (respons.statusCode != HttpStatus.OK) {
                throw RuntimeException("Kall mot aareg feiler med HTTP-${respons.statusCode}")
            }
            respons.body!!
        } catch (exception: RestClientException) {
            logger.error("Feil ved oppslag mot Aareg Arbeidsforhold: ", exception.message)
            throw RuntimeException(" Aareg Exception: $exception")
        }
    }

    fun hentOVersiktOverAntallArbeidsforholdForOpplysningspliktigFraAAReg(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String?,
        idPortenToken: String
    ): Array<OversiktOverArbeidsgiver> {
        val url = aaregArbeidsgiverOversiktUrl
        val entity: HttpEntity<String> = getRequestEntity(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken)
        return try {
            val respons = restTemplate.exchange(
                url,
                HttpMethod.GET, entity, Array<OversiktOverArbeidsgiver>::class.java
            )
            if (respons.statusCode != HttpStatus.OK) {
                val message = "Kall mot aareg feiler med HTTP-" + respons.statusCode
                throw RuntimeException(message)
            }
            respons.body!!
        } catch (exception: RestClientException) {
            logger.error("Feiler ved å hente arbeidsgiveroversikt fra Aareg ", exception.message)
            throw RuntimeException(" Aareg Exception: $exception")
        }
    }

    private fun getRequestEntity(
        bedriftsnr: String,
        juridiskEnhetOrgnr: String?,
        idPortenToken: String
    ): HttpEntity<String> {
        val appName = "srvditt-nav-arbeid"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers["Authorization"] = "Bearer $idPortenToken"
        headers["Nav-Call-Id"] = appName
        headers["Nav-Arbeidsgiverident"] = bedriftsnr
        headers["Nav-Opplysningspliktigident"] = juridiskEnhetOrgnr
        headers["Nav-Consumer-Token"] = stsClient.token?.access_token
        return HttpEntity(headers)
    }
}

