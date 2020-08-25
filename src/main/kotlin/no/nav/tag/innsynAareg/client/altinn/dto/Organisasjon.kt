package no.nav.tag.innsynAareg.client.altinn.dto

class Organisasjon(
    var Name: String? = null,
    var ParentOrganizationNumber: String? = null,
    var OrganizationNumber: String? = null,
    var OrganizationForm: String? = null,
    var Status: String? = null,
    var Type: String? = null
) {
    override fun toString(): String {
        return "Organisasjon(Name=$Name, ParentOrganizationNumber=$ParentOrganizationNumber, OrganizationNumber=$OrganizationNumber, OrganizationForm=$OrganizationForm, Status=$Status, Type=$Type)"
    }
}