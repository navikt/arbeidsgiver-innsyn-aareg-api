package no.nav.tag.innsynAareg.models.Yrkeskoderespons

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Sprak {
    @JsonProperty("term")
    private val term: String? = null
    @JsonProperty("tekst")
    private val tekst: String? = null
}