package no.nav.tag.innsynAareg.models.yrkeskoder

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class Yrkeskode {
    @JsonProperty("gyldigFra")
    val gyldigFra: String? = null

    @JsonProperty("gyldigTil")
    val gyldigTil: String? = null

    @JsonProperty("beskrivelser")
    val beskrivelser: Beskrivelser? = null
}