package no.nav.tag.innsynAareg.models.altinn

data class Organisasjon (var Name: String?,
                         var Type: String?,
                         var ParentOrganizationNumber: String?,
                         var OrganizationNumber: String?,
                         var OrganizationForm: String?,
                         var Status: String?
    ) {
    constructor() {
        Name = null;
        Type = null;
        ParentOrganizationNumber = null;
        OrganizationForm = null;
        Status = null;

    }
}
