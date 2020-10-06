package no.nav.tag.innsynAareg.client.aareg.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

val varselKodeOppslag: HashMap<String, String> = hashMapOf(
    "ERKONK" to "Kontroller sluttdatoen. NAV har satt samme sluttdato som konkursåpningsdato i Konkursregisteret.",
    "EROPPH" to "Kontroller sluttdatoen. NAV har satt samme sluttdato som datoen foretaket opphørte i Enhetsregisteret.",
    "ERVIRK" to "Kontroller sluttdatoen. NAV har satt samme sluttdato som datoen da foretaket ble overdratt til en annen juridisk enhet i Enhetsregisteret.",
    "IBARBG" to "Kontroller sluttdatoen. Du har ikke bekreftet arbeidsforholdet. NAV har satt sluttdato til siste dato i den kalendermåneden du sist bekreftet arbeidsforholdet.",
    "IBKAOR" to "Maskinell sluttdato: Ikke bekreftet i a-ordningen",
    "PPIDHI" to "NAV har slått sammen denne permitteringen/permisjonen med en annen da opplysningene er så like at vi tolker det som en og samme. Hvis du tror det er feil, sjekk at du ikke savner en permittering eller permisjon på dette arbeidsforholdet.",
    "NAVEND" to "NAV har opprettet eller endret arbeidsforholdet",
    "IBPPAG" to "Kontroller sluttdatoen. Du har ikke bekreftet permisjon/permitteringen. NAV har satt sluttdato til siste dato i den kalendermåneden du sist bekreftet opplysningen.",
    "AFIDHI" to "NAV har slått sammen dette arbeidsforholdet med et annet da opplysningene er så like at vi tolker det som ett og samme arbeidsforhold. Hvis du tror det er feil, sjekk at du ikke savner et tidligere arbeidsforhold"
)

data class Arbeidsgiver(
    val type: String
)

data class Arbeidstaker(
    val type: String,
    val aktoerId: String?,
    val offentligIdent: String?,
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

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArbeidsForhold(
    val ansattFom: String,
    val ansattTom: String?,
    val arbeidsgiver: Arbeidsgiver,
    var arbeidstaker: Arbeidstaker,
    val innrapportertEtterAOrdningen: String,
    val navArbeidsforholdId: String,
    val opplysningspliktig: Opplysningspliktig,
    val sistBekreftet: String,
    val stillingsprosent: String?,
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
