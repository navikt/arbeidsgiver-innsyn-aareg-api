package no.nav.tag.innsynAareg.client.yrkeskoder.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class Beskrivelser {
    @JsonProperty("nb")
    val nb: Sprak? = null
}