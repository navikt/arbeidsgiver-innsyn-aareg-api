package no.nav.tag.innsynAareg.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OversiktOverArbeidsgiver (val arbeidsgiver: OversiktArbeidsgiver, val aktiveArbeidsforhold: Int,val inaktiveArbeidsforhold: Int )
data class OversiktArbeidsgiver(@JsonProperty("type") val type: String,val organisasjonsnummer: String )
