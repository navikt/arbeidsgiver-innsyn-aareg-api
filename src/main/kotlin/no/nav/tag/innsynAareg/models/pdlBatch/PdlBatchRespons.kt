package no.nav.tag.innsynAareg.models.pdlBatch
import java.util.*

data class PdlBatchRespons(
    val data: Data,
    val errors: ArrayList<Error?>?
)