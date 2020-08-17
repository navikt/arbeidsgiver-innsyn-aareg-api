package no.nav.tag.innsynAareg.client.enhetsregisteret.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import kotlin.collections.LinkedHashMap

@JsonIgnoreProperties(ignoreUnknown = true)
class EnhetsRegisterOrg {
    @JsonProperty("organisasjonsnummer")
    val Organisasjonsnummer: String? = null

    @JsonProperty("navn")
    val Navn: Navn? = null

    var orgTre: Map<String, Any> = LinkedHashMap()

    var bestaarAvOrganisasjonsledd: ArrayList<BestaarAvOrganisasjonsledd>? = ArrayList()

    @JsonProperty("ansatte")
    var ansatte: ArrayList<Ansatte> = ArrayList<Ansatte>()
    @JsonProperty("driverVirksomheter")
    val driverVirksomheter: List<EnhetsRegisterOrg>? = null
}

class Ansatte {
    @JsonProperty("antall")
    var antall: Int? = null
}

class Navn {
    @JsonProperty("redigertnavn")
    val redigertnavn: String? = null
}

class BestaarAvOrganisasjonsledd {
    var organisasjonsledd: Organisasjoneledd? = null
}

class InngaarIJuridiskEnheter {
    val organisasjonsnummer: String? = null
}

class Organisasjoneledd {
    var organisasjonsnummer: String? = null
    var organisasjonsleddOver: ArrayList<OrganisasjonsleddOver>? = null
    var inngaarIJuridiskEnheter: ArrayList<InngaarIJuridiskEnheter>? = null
}

class OrganisasjonsleddOver {
    var organisasjonsledd: Organisasjoneledd? = null
}

