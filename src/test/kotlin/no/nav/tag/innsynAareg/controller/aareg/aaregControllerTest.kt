package no.nav.tag.innsynAareg.controller.aareg

import no.nav.tag.innsynAareg.controller.AaregController
import no.nav.tag.innsynAareg.service.aareg.AaregException
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner


@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.port=8082"])
class AAregControllerTest {
    @Autowired
    lateinit var aAregController: AaregController

    @Test(expected = AaregException::class)
    fun whenExceptionThrown_thenExpectationSatisfied() {
        aAregController.hentArbeidsforhold("910825517", "132", "132")
    }

    @Test
    fun hentAntallArbeidsforhold() {
        val responsMedInnhold = aAregController.hentAntallArbeidsforhold("910825518", "132", "132")
        Assert.assertEquals(Pair("983887457", 5), responsMedInnhold)
    }
}