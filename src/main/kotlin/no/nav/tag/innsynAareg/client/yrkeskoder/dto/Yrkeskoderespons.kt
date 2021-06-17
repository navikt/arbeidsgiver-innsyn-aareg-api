package no.nav.tag.innsynAareg.client.yrkeskoder.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Yrkeskoderespons(
    val betydninger : Map<String, List<Yrkeskode>> = hashMapOf()
)