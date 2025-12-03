package no.nav.tag.innsynAareg.client.tilgangskontroll

import no.nav.tag.innsynAareg.client.altinn.AltinnCacheConfig.Companion.ALTINN_TJENESTE_CACHE
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.models.AltinnIngenRettigheter
import no.nav.tag.innsynAareg.models.AltinnOppslagResultat
import no.nav.tag.innsynAareg.models.AltinnOppslagVellykket
import no.nav.tag.innsynAareg.service.tokenExchange.TokenExchangeClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.RequestEntity.method
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class TilgangskontrollClient(
    private val restTemplate: RestTemplate,
    private val tokenExchangeClient: TokenExchangeClient,
) {

    @Value("\${aareg.tilgangskontroll.alle.organisasjoner.url}")
    lateinit var alleOrganisasjonerUrl: String

    @Value("\${aareg.tilgangskontroll.organisasjoner.url}")
    lateinit var organisasjonerUrl: String

    @Value("\${aareg.tilgangskontroll.scope}")
    lateinit var tilgangskontrollScope: String

    val logger = LoggerFactory.getLogger(TilgangskontrollClient::class.java)!!

    @Cacheable(ALTINN_TJENESTE_CACHE)
    fun hentOrganisasjonerBasertPaRettigheter(): AltinnOppslagResultat  = run {
        restTemplate.exchange(
            method(HttpMethod.GET, organisasjonerUrl)
                .medHeadere().build(),
            TilgangskontrollResponse::class.java
        ).body!!
    }

    fun hentOrganisasjoner(): AltinnOppslagResultat = run {
        restTemplate.exchange(
            method(HttpMethod.GET, alleOrganisasjonerUrl)
                .medHeadere().build(),
            TilgangskontrollResponse::class.java
        ).body!!
    }

    private fun run(action: () -> TilgangskontrollResponse) =
        try {
            action()
                .organisasjoner
                .map {
                    Organisasjon(
                        Name = it.Name,
                        ParentOrganizationNumber = it.ParentOrganizationNumber,
                        OrganizationNumber = it.OrganizationNumber,
                        OrganizationForm = it.OrganizationForm,
                        Status = it.Status,
                        Type = it.Type
                    )
                }
                .let { AltinnOppslagVellykket(it) }
        } catch (error: Exception) {
            if (error.message?.contains("403") == true)
                AltinnIngenRettigheter
            else
                throw RuntimeException("Klarte ikke hente organisasjoner fra aareg-tilgangskontroll. $error", error)
        }

    private fun RequestEntity.BodyBuilder.medHeadere(): RequestEntity.BodyBuilder = headers {
        it.contentType = MediaType.APPLICATION_JSON
        it["Authorization"] = "Bearer ${tokenExchangeClient.exchangeToken(tilgangskontrollScope).access_token}"
    }
}
