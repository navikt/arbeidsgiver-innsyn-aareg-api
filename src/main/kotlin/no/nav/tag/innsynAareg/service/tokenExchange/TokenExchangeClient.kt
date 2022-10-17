package no.nav.tag.innsynAareg.service.tokenExchange


import no.nav.tag.innsynAareg.client.RetryInterceptor
import no.nav.tag.innsynAareg.service.tokenExchange.TokenXProperties.Companion.CLIENT_ASSERTION_TYPE
import no.nav.tag.innsynAareg.service.tokenExchange.TokenXProperties.Companion.GRANT_TYPE
import no.nav.tag.innsynAareg.service.tokenExchange.TokenXProperties.Companion.SUBJECT_TOKEN_TYPE
import no.nav.tag.innsynAareg.utils.AutentisertBruker
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

interface TokenExchangeClient{
    fun exchangeToken(audience: String): TokenXToken
}

@Profile("dev", "prod")
@Component
class TokenExchangeClientImpl internal constructor (
    val properties: TokenXProperties,
    val clientAssertionTokenFactory: ClientAssertionTokenFactory,
    val autentisertBruker: AutentisertBruker,
    restTemplateBuilder: RestTemplateBuilder,
) : TokenExchangeClient{
    private val restTemplate = restTemplateBuilder
        .additionalInterceptors(
            RetryInterceptor(3, 250L, java.net.SocketException::class.java)
        ).build()


    override fun exchangeToken(audience: String): TokenXToken {
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

@Profile("local", "labs")
@Component
class TokenExchangeClientStub: TokenExchangeClient {
    override fun exchangeToken(audience: String): TokenXToken {
        return TokenXToken("", "", "", 2)
    }
}

data class TokenXToken (
    val access_token: String,
    val issued_token_type: String,
    val token_type: String,
    val expires_in: Int,
)