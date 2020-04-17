package no.nav.tag.innsynAareg.service.yrkeskoder

import no.nav.tag.innsynAareg.service.pdl.PdlService
import no.nav.tag.innsynAareg.service.sts.STSClient
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.port=8082"])
internal class PdlServiceTest() {
    @Autowired
    lateinit var pdlService: PdlService;

    private val FNR = "123"

    @Mock
    private val restTemplate: RestTemplate? = null


    @Mock
    var stsClient: STSClient? = null

    @Test
    fun hentNavn() {
        val respons = pdlService.hentNavnMedFnr("123123")
        val navn: String = respons
        Assert.assertEquals("Kunne ikke hente navn", navn);
    }

}