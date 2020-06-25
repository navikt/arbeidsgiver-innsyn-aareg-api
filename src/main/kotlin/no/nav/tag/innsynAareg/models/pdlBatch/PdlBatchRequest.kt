package no.nav.tag.innsynAareg.models.pdlBatch

import no.nav.tag.innsynAareg.models.pdlPerson.HentPerson
import no.nav.tag.innsynAareg.models.pdlPerson.Variables

data class PdlBatchRequest (val variables: Variables?, val query: String? = null)