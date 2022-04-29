package no.nav.tag.innsynAareg.service.yrkeskoder

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkClient
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableMockOAuth2Server
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.port=8082"])
internal class YrkeskodeverkClientTest {
    @Autowired
    lateinit var yrkeskodeservice: YrkeskodeverkClient

    @Test
    fun hentBetydningerAvYrkeskoder() {
        val respons = yrkeskodeservice.hentBetydningAvYrkeskoder()
        val betydning = respons.betydningPåYrke("1227184")
        Assert.assertEquals("PLANSJEF (OFFENTLIG VIRKSOMHET)", betydning)
    }

    @Test
    fun `Ukjent yrkeskode gir passende feilmelding`() {
        val respons = yrkeskodeservice.hentBetydningAvYrkeskoder()
        val betydning = respons.betydningPåYrke("111111111111111")
        Assert.assertEquals("Fant ikke yrkesbeskrivelse", betydning)
    }

}