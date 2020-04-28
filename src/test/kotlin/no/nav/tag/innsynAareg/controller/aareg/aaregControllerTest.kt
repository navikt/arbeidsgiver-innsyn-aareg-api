package no.nav.tag.innsynAareg.controller.aareg


import no.nav.tag.innsynAareg.controller.AaregController
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
    lateinit var aAregController: AaregController;

    @Test
    fun hentArbeidsforhold() {
        val tomRespons = aAregController.hentArbeidsforhold("910825517", "132", "132")
        Assert.assertEquals(0, tomRespons?.arbeidsforholdoversikter?.size)
        val responsMedInnhold = aAregController.hentArbeidsforhold("910825518", "132", "132")
        Assert.assertEquals(13, responsMedInnhold?.arbeidsforholdoversikter?.size)
    }
}