package no.nav.tag.innsynAareg.client.enhetsregisteret.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OrganisasjonFraEreg(
        val organisasjonsnummer: String,
        val bestaarAvOrganisasjonsledd: List<Organisasjoneledd>?,
        val organisasjonsleddOver: List<Organisasjoneledd>?,
        val inngaarIJuridiskEnheter: List<OrganisasjonFraEreg>?,
        val gyldighetsperiode: Gyldighetsperiode?,
        val navn: Navn?,
        val driverVirksomheter: List<OrganisasjonFraEreg>?
)

data class Organisasjoneledd(val organisasjonsledd: OrganisasjonFraEreg)

data class Navn(val redigertnavn: String?)

data class Gyldighetsperiode(val fom: String?, val tom: String?);