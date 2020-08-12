package no.nav.tag.innsynAareg.client.pdl.dto

import java.util.*

data class PdlBatchRespons(
    val data: Data,
    val errors: ArrayList<Error?>?
)