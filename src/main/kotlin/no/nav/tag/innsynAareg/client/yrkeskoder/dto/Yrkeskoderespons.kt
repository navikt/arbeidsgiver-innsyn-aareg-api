package no.nav.tag.innsynAareg.client.yrkeskoder.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Yrkeskoderespons {
    val betydninger = hashMapOf<String, List<Yrkeskode>>()
}