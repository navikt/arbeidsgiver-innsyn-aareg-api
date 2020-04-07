package no.nav.tag.innsynAareg.models.pdlPerson

import lombok.Value

@Value
class PdlRequest {
    private val query: String? = null
    private val variables: Variables? = null
}