package no.nav.tag.innsynAareg.client.azure

import java.time.LocalDateTime

@Suppress("Unused") /* dto */
data class AzureToken(
    var access_token: String,
    var expires_in: LocalDateTime
)