package no.nav.tag.innsynAareg.client.aareg

import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsgiver
import no.nav.tag.innsynAareg.client.sts.STSClient
import no.nav.tag.innsynAareg.models.ArbeidsforholdFunnet
import no.nav.tag.innsynAareg.models.ArbeidsforholdOppslagResultat
import no.nav.tag.innsynAareg.models.IngenRettigheter
import no.nav.tag.innsynAareg.service.tokenExchange.TokenExchangeClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.RequestEntity.method
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

/* Swagger for API-et (krever VDI): * https://modapp-q1.adeo.no/aareg-services/api/swagger-ui/index.html#/
 * Repo for API-et: https://github.com/navikt/aareg-services
 **/
@Service
class AaregClient(
    private val tokenExchangeClient: TokenExchangeClient,
    private val restTemplate: RestTemplate,
    private val stsClient: STSClient,
) {
    @Value("\${aareg.audience}")
    lateinit var audience: String

    @Value("\${aareg.aaregArbeidsforhold}")
    lateinit var aaregArbeidsforholdUrl: String

    @Value("\${aareg.aaregArbeidsgivere}")
    lateinit var aaregArbeidsgiverOversiktUrl: String

    val logger = LoggerFactory.getLogger(AaregClient::class.java)!!

    fun hentArbeidsforhold(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String,
    ): ArbeidsforholdOppslagResultat {
        return try {
            ArbeidsforholdFunnet(
                restTemplate.exchange(
                    method(HttpMethod.GET, aaregArbeidsforholdUrl)
                        .medHeadere(bedriftsnr, overOrdnetEnhetOrgnr).build(),
                    OversiktOverArbeidsForhold::class.java
                ).body!!
            )
        } catch (exception: HttpClientErrorException.Forbidden) {
            logger.warn("Forbidden fra aareg")
            return IngenRettigheter
        }
    }

    fun antallArbeidsforholdForOpplysningspliktig(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String?,
    ): Int? {
        val oversikt = hentOversiktOverAntallArbeidsforholdForOpplysningspliktig(
            bedriftsnr,
            overOrdnetEnhetOrgnr,
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
    ): Array<OversiktOverArbeidsgiver> {
        return restTemplate.exchange(
            method(HttpMethod.GET, aaregArbeidsgiverOversiktUrl)
                .medHeadere(bedriftsnr, overOrdnetEnhetOrgnr).build(),
            Array<OversiktOverArbeidsgiver>::class.java
        ).body!!
    }

    private fun RequestEntity.BodyBuilder.medHeadere(
        bedriftsnr: String,
        overOrdnetEnhetOrgnr: String?,
    ): RequestEntity.BodyBuilder {
        val token = tokenExchangeClient.exchangeToken(audience).access_token
        return headers {
            it.contentType = MediaType.APPLICATION_FORM_URLENCODED
            it["Authorization"] = "Bearer $token"
            it["Nav-Call-Id"] = "srvditt-nav-arbeid"
            it["Nav-Arbeidsgiverident"] = bedriftsnr
            it["Nav-Opplysningspliktigident"] = overOrdnetEnhetOrgnr
            it["Nav-Consumer-Token"] = stsClient.token.access_token
        }
    }
}

