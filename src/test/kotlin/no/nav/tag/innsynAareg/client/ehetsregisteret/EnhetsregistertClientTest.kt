package no.nav.tag.innsynAareg.client.ehetsregisteret

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
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
class EnhetsregistertClientTest {
    @Autowired
    lateinit var enhetsregisteretClient: EnhetsregisteretClient

    @Test
    fun getToken() {
        val result = enhetsregisteretClient.finnTidligereVirksomheter("910825518", "123")
        Assert.assertNotNull (result)
        Assert.assertEquals(2, result.size)
    }
}