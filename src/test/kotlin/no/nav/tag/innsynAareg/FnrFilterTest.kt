package no.nav.tag.innsynAareg

import junit.framework.Assert.assertEquals
import org.junit.Test

class FnrFilterTest {


    fun filtrer(tekst: String): String {
        return tekst.replace(Regex("""\d{11}"""), "***********")
    }

    @Test
    fun filtererOrgnr() {
        val tekst = "Hallo 12345678901 bla bla 98765432100"
        val ønsket = "Hallo *********** bla bla ***********"

        assertEquals(ønsket, filtrer(tekst))
    }
}