package no.nav.tag.innsynAareg.models.altinn

data class Organisasjon (var Name: String?,
                         val Type: String?,
                         val ParentOrganizationNumber: String?,
                         val OrganizationNumber: String?,
                         val OrganizationForm: String?,
                         val Status: String?
    )
