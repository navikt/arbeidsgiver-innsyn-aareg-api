package no.nav.tag.innsynAareg.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class OversiktOverArbeidsgiver (val arbeidsgiver: OversiktArbeidsgiver, val aktiveArbeidsforhold: Number,val inaktiveArbeidsforhold: Number )
data class OversiktArbeidsgiver(@JsonProperty("type") val type: String,val organisasjonsnummer: String )
