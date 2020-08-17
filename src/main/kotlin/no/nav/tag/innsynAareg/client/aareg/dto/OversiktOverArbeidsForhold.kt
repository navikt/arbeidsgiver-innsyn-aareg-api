package no.nav.tag.innsynAareg.client.aareg.dto

val varselKodeOppslag: HashMap<String, String> = hashMapOf(
    "ERKONK" to "Maskinell sluttdato: Konkurs",
    "EROPPH" to "Maskinell sluttdato: Opphørt i Enhetsregisteret",
    "ERVIRK" to "Maskinell sluttdato: Virksomhetoverdragelse",
    "IBARBG" to "Maskinell sluttdato: Ikke bekreftet",
    "IBKAOR" to "Maskinell sluttdato: Ikke bekreftet i a-ordningen"
)

data class Arbeidsgiver(
    val type: String
)

data class Arbeidstaker(
    val type: String,
    val aktoerId: String,
    val offentligIdent: String,
    var navn: String?
)

data class Opplysningspliktig(
    val type: String
)

data class ArbeidsforholdVarsel(
    val entitet: String
) {
    var varslingskode: String? = null
        set(value) {
            field = value
            this.varslingskodeForklaring = varselKodeOppslag[varslingskode]
        }

    var varslingskodeForklaring: String? = null
}

data class ArbeidsForhold(
    val ansattFom: String,
    val ansattTom: String?,
    val arbeidsgiver: Arbeidsgiver,
    var arbeidstaker: Arbeidstaker,
    val innrapportertEtterAOrdningen: String,
    val navArbeidsforholdId: String,
    val opplysningspliktig: Opplysningspliktig,
    val sistBekreftet: String,
    val stillingsprosent: String,
    val type: String,
    val varsler: List<ArbeidsforholdVarsel>?,
    val yrke: String,
    var yrkesbeskrivelse: String?
)

data class OversiktOverArbeidsForhold(
    val antall: Long?,
    val arbeidsforholdoversikter: List<ArbeidsForhold>?,
    val startrad: String?,
    val totalAntall: String?
)

sealed class AaregFeil
object IngenRettigheter : AaregFeil()
