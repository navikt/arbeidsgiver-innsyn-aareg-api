package no.nav.tag.innsynAareg.models.yrkeskoder

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class Sprak {
    @JsonProperty("term")
    val term: String? = null

    @JsonProperty("tekst")
    val tekst: String? = null
}