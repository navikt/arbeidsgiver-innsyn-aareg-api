package no.nav.tag.innsynAareg.client.aareg.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class Arbeidstaker(
    val offentligIdent: String,
    var navn: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArbeidsForhold(
    var arbeidstaker: Arbeidstaker,
    val yrke: String,
    var yrkesbeskrivelse: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class OversiktOverArbeidsForhold(
    val antall: Long?,
    val arbeidsforholdoversikter: List<ArbeidsForhold>?,
    val startrad: String?,
    val totalAntall: String?
)
