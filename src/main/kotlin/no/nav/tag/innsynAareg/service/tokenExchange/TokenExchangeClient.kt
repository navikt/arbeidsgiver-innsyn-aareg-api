package no.nav.tag.innsynAareg.service.tokenExchange


import no.nav.tag.innsynAareg.service.tokenExchange.TokenXProperties.Companion.CLIENT_ASSERTION_TYPE
import no.nav.tag.innsynAareg.service.tokenExchange.TokenXProperties.Companion.GRANT_TYPE
import no.nav.tag.innsynAareg.service.tokenExchange.TokenXProperties.Companion.SUBJECT_TOKEN_TYPE
import no.nav.tag.innsynAareg.utils.AutentisertBruker
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate


@Profile("local", "dev", "prod")
@Component
class TokenExchangeClient internal constructor(
    val properties: TokenXProperties,
    val clientAssertionTokenFactory: ClientAssertionTokenFactory,
    val restTemplate: RestTemplate,
    val autentisertBruker: AutentisertBruker,
) {
    fun exchangeToken(audience: String): TokenXToken {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        val request: HttpEntity<MultiValueMap<String, String>> = HttpEntity<MultiValueMap<String, String>>(
            LinkedMultiValueMap(
                mapOf(
                    "grant_type" to listOf(GRANT_TYPE),
                    "client_assertion_type" to listOf(CLIENT_ASSERTION_TYPE),
                    "subject_token_type" to listOf(SUBJECT_TOKEN_TYPE),
                    "subject_token" to listOf(autentisertBruker.jwtToken),
                    "client_assertion" to listOf(clientAssertionTokenFactory.clientAssertion),
                    "audience" to listOf(audience),
                    "client_id" to listOf(properties.clientId)
                )
            ), headers
        )
        return restTemplate.postForEntity(properties.tokendingsUrl, request, TokenXToken::class.java)
            .body!!
    }
}

data class TokenXToken (
    val access_token: String,
    val issued_token_type: String,
    val token_type: String,
    val expires_in: Int,
)