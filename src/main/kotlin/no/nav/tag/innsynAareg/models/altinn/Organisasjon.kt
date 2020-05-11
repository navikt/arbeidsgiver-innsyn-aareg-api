package no.nav.tag.innsynAareg.models.altinn

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Data

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
class Organisasjon {
    private val Name: String? = null
    private val Type: String? = null
    private val ParentOrganizationNumber: String? = null
    private val OrganizationNumber: String? = null
    private val OrganizationForm: String? = null
    private val Status: String? = null
}