package no.nav.tag.innsynAareg.models.yrkeskoder

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Yrkeskoderespons {
    val betydninger = hashMapOf<String, List<Yrkeskode>>()
}