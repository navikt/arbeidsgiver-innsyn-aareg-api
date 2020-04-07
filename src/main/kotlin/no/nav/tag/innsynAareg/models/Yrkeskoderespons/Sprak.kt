package no.nav.tag.innsynAareg.models.Yrkeskoderespons

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Sprak {
    @JsonProperty("term")
    val term: String? = null
    @JsonProperty("tekst")
    val tekst: String? = null
}