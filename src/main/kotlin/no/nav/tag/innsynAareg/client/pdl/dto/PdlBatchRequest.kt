package no.nav.tag.innsynAareg.client.pdl.dto

data class PdlBatchRequest(
    val query: String? = null,
    val variables: Variables?
)