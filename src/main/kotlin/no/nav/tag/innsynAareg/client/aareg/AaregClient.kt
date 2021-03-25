package no.nav.tag.innsynAareg.client.aareg

import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsgiver
import no.nav.tag.innsynAareg.client.sts.STSClient
import no.nav.tag.innsynAareg.models.ArbeidsforholdFunnet
import no.nav.tag.innsynAareg.models.ArbeidsforholdOppslagResultat
import no.nav.tag.innsynAareg.models.IngenRettigheter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate

@Service
class AaregClient(
    private val restTemplate: RestTemplate,
    private val stsClient: STSClient
) {
    @Value("\${aareg.aaregArbeidsforhold}")
    lateinit var aaregArbeidsforholdUrl: String

    @Value("\${aareg.aaregArbeidsgivere}")
    lateinit var aaregArbeidsgiverOversiktUrl: String

    val logger = LoggerFactory.getLogger(AaregClient::class.java)!!

    fun hentArbeidsforhold(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String,
        idPortenToken: String
    ): ArbeidsforholdOppslagResultat {
        val entity: HttpEntity<String> = getRequestEntity(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken)
        return try {
            val respons = restTemplate.exchange(
                aaregArbeidsforholdUrl,
                HttpMethod.GET,
                entity,
                OversiktOverArbeidsForhold::class.java
            )
            when (respons.statusCode) {
                HttpStatus.OK -> ArbeidsforholdFunnet(respons.body!!)
                else -> throw RuntimeException("Kall mot aareg feiler med HTTP-${respons.statusCode}")
            }
        } catch (exception: RestClientException) {
            logger.error("Feil ved oppslag mot Aareg Arbeidsforhold.", exception)
            if (exception is RestClientResponseException && exception.rawStatusCode == 403) {
                return IngenRettigheter
            }
            throw RuntimeException(" Aareg Exception: $exception")
        }
    }

    fun antallArbeidsforholdForOpplysningspliktig(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String?,
        idPortenToken: String
    ): Int? {
        val oversikt = hentOversiktOverAntallArbeidsforholdForOpplysningspliktig(
            bedriftsnr,
            overOrdnetEnhetOrgnr,
            idPortenToken
        )
        if (oversikt.isEmpty()) {
            logger.info("Aareg oversikt over arbeidsgiver respons er tom")
        }
        return oversikt.find { it.arbeidsgiver.organisasjonsnummer == bedriftsnr }
            ?.let { it.aktiveArbeidsforhold + it.inaktiveArbeidsforhold }
    }

    private fun hentOversiktOverAntallArbeidsforholdForOpplysningspliktig(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String?,
        idPortenToken: String
    ): Array<OversiktOverArbeidsgiver> {
        val entity: HttpEntity<String> = getRequestEntity(bedriftsnr, overOrdnetEnhetOrgnr, idPortenToken)
        return try {
            val respons = restTemplate.exchange(
                aaregArbeidsgiverOversiktUrl,
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
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers["Authorization"] = "Bearer $idPortenToken"
        headers["Nav-Call-Id"] = "srvditt-nav-arbeid"
        headers["Nav-Arbeidsgiverident"] = bedriftsnr
        headers["Nav-Opplysningspliktigident"] = juridiskEnhetOrgnr
        headers["Nav-Consumer-Token"] = stsClient.token?.access_token
        return HttpEntity(headers)
    }

}

