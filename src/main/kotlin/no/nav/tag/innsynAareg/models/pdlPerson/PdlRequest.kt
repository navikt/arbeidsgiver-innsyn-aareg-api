package no.nav.tag.innsynAareg.models.pdlPerson

import lombok.Value

@Value
data class PdlRequest(val query: String?, val variable: Variables);