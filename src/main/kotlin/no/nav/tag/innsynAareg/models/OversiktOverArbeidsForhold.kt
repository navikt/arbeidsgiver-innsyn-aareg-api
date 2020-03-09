package no.nav.tag.innsynAareg.models

val varselKodeOppslag: HashMap<String, String> = hashMapOf(
        "ERKONK" to "Maskinell sluttdato: Konkurs",
        "EROPPH" to "Maskinell sluttdato: Opphørt i Enhetsregisteret",
        "ERVIRK" to "Maskinell sluttdato: Virksomhetoverdragelse",
        "IBARBG" to "Maskinell sluttdato: Ikke bekreftet",
        "IBKAOR" to "Maskinell sluttdato: Ikke bekreftet i a-ordningen"
        )

data class Arbeidsgiver(
        val type:String
)
data class Arbeidstaker(
        val type:String,
        val aktoerId:String,
        val offegtligId: String?,
        val navn:String?
)
data class Opplysningspliktig(
        val type:String
)
data class ArbeidsforholdVarsel(
        val entitet:String
){
        var varslingskode:String? =null
                set(value){
                        field=value
                        this.varslingskodeForklaring=varselKodeOppslag.get(varslingskode)
                }
        var varslingskodeForklaring:String?=null

}

data class ArbeidsForhold(
        val ansattFom:String,
        val ansattTom:String?,
        val arbeidsgiver: Arbeidsgiver,
        val arbeidstaker:Arbeidstaker,
        val innrapportertEtterAOrdningen:String,
        val navArbeidsforholdId:String,
        val opplysningspliktig: Opplysningspliktig,
        val sistBekreftet:String,
        val stillingsprosent:String,
        val type:String,
        val varsler:Array<ArbeidsforholdVarsel>?,
        val yrke:String,
        val yrkesbeskrivelse:String?
        )

data class OversiktOverArbeidsForhold(
        val antall:Long,
        val arbeidsforholdoversikter:Array<ArbeidsForhold>,
        val startrad:String,
        val totalAntall:String
        )
