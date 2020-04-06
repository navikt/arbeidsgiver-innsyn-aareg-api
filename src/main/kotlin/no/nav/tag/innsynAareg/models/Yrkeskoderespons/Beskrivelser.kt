package no.nav.tag.innsynAareg.models.Yrkeskoderespons

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Beskrivelser {
    @JsonProperty("nn")
    private val nn: Sprak? = null
    @JsonProperty("nb")
    private val nb: Sprak? = null
}