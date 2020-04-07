package no.nav.tag.innsynAareg.models.Yrkeskoderespons

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Beskrivelser {
    @JsonProperty("nn")
    val nn: Sprak? = null
    @JsonProperty("nb")
    val nb: Sprak? = null
}