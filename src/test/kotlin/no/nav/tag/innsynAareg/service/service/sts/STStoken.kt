package no.nav.tag.innsynAareg.service

import no.nav.tag.innsynAareg.service.sts.STSClient
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

import org.junit.Assert.*

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("dev")
@TestPropertySource(properties = ["mock.port=8082"])
class AktorClientTest {
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired

    private val stsClient: STSClient? = null;

    @Test
    fun getstsToken() {

        //val result = STSClient.STStoken
        //assertEquals(result, "Bearer");
    }
}