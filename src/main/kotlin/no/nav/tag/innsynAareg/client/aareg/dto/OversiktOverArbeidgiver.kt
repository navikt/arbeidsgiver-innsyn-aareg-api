package no.nav.tag.innsynAareg.client.aareg.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OversiktOverArbeidsgiver(
    val arbeidsgiver: OversiktArbeidsgiver,
    val aktiveArbeidsforhold: Int,
    val inaktiveArbeidsforhold: Int
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OversiktArbeidsgiver(
    val type: String,
    val organisasjonsnummer: String
)
