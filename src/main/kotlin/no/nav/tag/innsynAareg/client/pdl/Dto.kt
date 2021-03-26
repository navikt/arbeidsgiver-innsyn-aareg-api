package no.nav.tag.innsynAareg.client.pdl

const val identer = "\$identer" /* workaround fordi man ikke kan escape i block quote */
const val hentPersonBolkQuery =
"""
query($identer: [ID!]!) {
    hentPersonBolk(identer: $identer) {
        ident
        person {
            navn {
                fornavn
                mellomnavn
                etternavn
            }
        }
        code
    }
}
"""

data class HentPersonBolkRequest(
    val query: String = hentPersonBolkQuery,
    val variables: Variables
) {
    constructor(identer: List<String>): this(variables = Variables(identer))

    data class Variables(
        val identer: List<String>
    )
}

data class HentPersonBolkResponse(
    val data: Data,
    val errors: List<Error?>?
) {
    data class Error(val message: String)
    data class Data(val hentPersonBolk: List<PersonlisteElement>)
}

data class PersonlisteElement(
    val ident: String? = null,
    val person: Person? = null,
    val code: String? = null
)

data class Person(
    val navn: List<Navn>?
)

data class Navn(
    val fornavn: String?,
    val mellomNavn: String?,
    val etternavn: String?
)
