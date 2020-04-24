package no.nav.tag.innsynAareg.models.pdlPerson

import lombok.Value

@Value
class Variables (val ident: String) {
    fun getVariable(): String {
        return ident;
    }
}
