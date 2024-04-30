package no.nav.tag.innsynAareg.client.azure

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class AccessTokenResponse(
    @field:JsonProperty("expires_in") var expiresIn: Long,
    @field:JsonProperty("access_token") var accessToken: String
)
