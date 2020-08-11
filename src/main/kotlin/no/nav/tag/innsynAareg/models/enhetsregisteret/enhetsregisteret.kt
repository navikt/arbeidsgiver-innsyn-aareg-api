package no.nav.tag.innsynAareg.models.enhetsregisteret

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data
import java.util.*
import kotlin.collections.LinkedHashMap


@JsonIgnoreProperties(ignoreUnknown = true)
class EnhetsRegisterOrg {
    @JsonProperty("organisasjonsnummer")
    private val organisasjonsnummer: String? = null

    var orgTre: Map<String, Any> = LinkedHashMap()

    var bestaarAvOrganisasjonsledd: ArrayList<BestaarAvOrganisasjonsledd>? = ArrayList()

    @JsonProperty("ansatte")
    var ansatte: ArrayList<Ansatte> = ArrayList<Ansatte>()
}

class Ansatte {
    @JsonProperty("antall")
    var antall: Int? = null
}

class BestaarAvOrganisasjonsledd {
    var organisasjonsledd: Organisasjoneledd? = null
}


class InngaarIJuridiskEnheter {
    val organisasjonsnummer: String? = null
}


@Data
class Organisasjoneledd {
    var organisasjonsnummer: String? = null
    var organisasjonsleddOver: ArrayList<OrganisasjonsleddOver>? = null
    var inngaarIJuridiskEnheter: ArrayList<InngaarIJuridiskEnheter>? = null
}

@Data
class OrganisasjonsleddOver {
    var organisasjonsledd: Organisasjoneledd? = null
}

