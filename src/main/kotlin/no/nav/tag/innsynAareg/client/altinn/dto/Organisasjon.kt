package no.nav.tag.innsynAareg.client.altinn.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Organisasjon(
    var Name: String? = null,
    var Type: String? = null
)