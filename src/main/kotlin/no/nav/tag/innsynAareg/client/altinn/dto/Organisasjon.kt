package no.nav.tag.innsynAareg.client.altinn.dto

data class Organisasjon(
    var Name: String? = null,
    var ParentOrganizationNumber: String? = null,
    var OrganizationNumber: String? = null,
    var OrganizationForm: String? = null,
    var Status: String? = null,
    var Type: String? = null
)