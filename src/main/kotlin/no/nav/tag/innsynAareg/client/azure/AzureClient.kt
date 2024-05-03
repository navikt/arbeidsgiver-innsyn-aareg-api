package no.nav.tag.innsynAareg.client.azure

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.util.*

@Component
class AzureClient @Autowired constructor(
    @Value("\${azure.tokenUrl}") private val tokenUrl: String,
    @Value("\${AZURE_APP_CLIENT_ID}") private val clientId: String,
    @Value("\${AZURE_APP_CLIENT_SECRET}") private val clientSecret: String,
    private val restTemplate: RestTemplate
) {
    private val log = LoggerFactory.getLogger(AzureClient::class.java)!!
    private val tokens: LinkedHashMap<String, AzureToken> = LinkedHashMap()

    fun getToken(scope: String): String {
        if (!tokens.containsKey(scope)) {
            updateToken(scope)
        }
        updateTokenIfNeeded(scope)
        return tokens.getValue(scope).access_token
    }

    private fun updateTokenIfNeeded(scope: String) {
        synchronized(this) {
            val token = tokens.getValue(scope)
            if (shouldRefresh(token.expires_in)) {
                updateToken(scope)
            }
        }
    }

    private fun updateToken(scope: String) {
        try {
            val formParameters = formParameters(scope)

            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            headers.accept = listOf(MediaType.APPLICATION_JSON)
            headers.setBasicAuth(clientId, clientSecret)

            val requestEntity = HttpEntity<MultiValueMap<String, String>>(formParameters, headers)

            val response =
                restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, AccessTokenResponse::class.java).body!!

            val token = AzureToken(response.accessToken, LocalDateTime.now().plusSeconds(response.expiresIn))

            tokens[scope] = token
        } catch (e: Exception) {
            log.error("Feil ved henting av token fra Azure. $e", e)
            throw RuntimeException("AG-ARBEIDSFORHOLD Klarte ikke hente token fra azure. $e", e)
        }
    }

    private fun shouldRefresh(expiry: LocalDateTime): Boolean {
        return Objects.isNull(expiry) || LocalDateTime.now().plusMinutes(1).isAfter(expiry)
    }

    private fun formParameters(scope: String): MultiValueMap<String, String> {
        val formParameters: MultiValueMap<String, String> = LinkedMultiValueMap()
        formParameters.add("grant_type", "client_credentials")
        formParameters.add("scope", scope)

        return formParameters
    }
}