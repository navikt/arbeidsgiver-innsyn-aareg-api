package no.nav.tag.innsynAareg.service.yrkeskoder

import no.nav.tag.innsynAareg.models.Yrkeskoderespons.Yrkeskoderespons

import org.junit.Assert
import org.springframework.beans.factory.annotation.Autowired

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.port=8082"])
internal class YrkeskodeverkServiceTest() {
    @Autowired
    lateinit var yrkeskodeservice: YrkeskodeverkService;

    @Test
    fun hentBetydningerAvYrkeskoder() {
        val respons: Yrkeskoderespons? = yrkeskodeservice.hentBetydningerAvYrkeskoder();
        Assert.assertEquals("hello ", respons.toString());

    }
}