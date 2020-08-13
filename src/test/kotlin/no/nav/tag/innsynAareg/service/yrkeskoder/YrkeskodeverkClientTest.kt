package no.nav.tag.innsynAareg.service.yrkeskoder

import no.nav.tag.innsynAareg.client.yrkeskoder.YrkeskodeverkClient
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
internal class YrkeskodeverkClientTest {
    @Autowired
    lateinit var yrkeskodeservice: YrkeskodeverkClient

    @Test
    fun hentBetydningerAvYrkeskoder() {
        val respons = yrkeskodeservice.hentBetydningerAvYrkeskoder()
        val betydning = respons?.betydninger?.get("1227184")?.get(0)?.beskrivelser?.nb?.tekst
        Assert.assertEquals("PLANSJEF (OFFENTLIG VIRKSOMHET)", betydning)
    }
}