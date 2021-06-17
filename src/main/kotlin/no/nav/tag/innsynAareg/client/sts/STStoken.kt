package no.nav.tag.innsynAareg.client.sts

@Suppress("Unused") /* dto */
data class STStoken(
    var access_token: String,
    var token_type: String? = null,
    var expires_in: Int = 0,
)

