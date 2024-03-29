package no.nav.tag.innsynAareg.controller.aareg
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.tag.innsynAareg.controller.AaregController
import no.nav.tag.innsynAareg.mockserver.ORGNR_HOVEDENHET_UTEN_AAREG_RETTIGHETER
import no.nav.tag.innsynAareg.mockserver.ORGNR_UNDERENHET_UTEN_AAREG_RETTIGHETER
import no.nav.tag.innsynAareg.utils.AutentisertBruker
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner


@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableMockOAuth2Server
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.port=8083"])
class AAregControllerTest {
    @Autowired
    lateinit var aAregController: AaregController

    @MockBean
    lateinit var autentisertBruker: AutentisertBruker

    @Before
    fun setUp() {
        Mockito.`when`(autentisertBruker.jwtToken).thenReturn("fake token")
    }

    @Test
    fun hentAntallArbeidsforhold() {
        val responsMedInnhold = aAregController.hentAntallArbeidsforhold("910825518", "132")
        Assert.assertEquals(Pair("983887457", 5), responsMedInnhold)
    }

    @Test
    fun `får 403 når vi ikke har tilgang`() {
        val response = aAregController.hentArbeidsforhold(
            ORGNR_UNDERENHET_UTEN_AAREG_RETTIGHETER,
            ORGNR_HOVEDENHET_UTEN_AAREG_RETTIGHETER
        )
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)

    }
}