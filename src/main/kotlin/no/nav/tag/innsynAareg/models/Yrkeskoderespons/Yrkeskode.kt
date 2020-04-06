package no.nav.tag.innsynAareg.models.Yrkeskoderespons

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Yrkeskode {
    @JsonProperty("gyldigFra")
    private val gyldigFra: String? = null
    @JsonProperty("gyldigTil")
    private val gyldigTil: String? = null
    @JsonProperty("beskrivelser")
    private val beskrivelser: Beskrivelser? = null
}