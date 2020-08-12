package no.nav.tag.innsynAareg.models.yrkeskoder

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class Beskrivelser {
    @JsonProperty("nn")
    val nn: Sprak? = null

    @JsonProperty("nb")
    val nb: Sprak? = null
}