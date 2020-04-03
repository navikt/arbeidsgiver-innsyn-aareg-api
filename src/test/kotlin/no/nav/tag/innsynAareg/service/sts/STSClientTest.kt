package no.nav.tag.innsynAareg.service.sts

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
internal class STSClientTest() {
    @Autowired
    lateinit var stsClient: STSClient

    @Test
    fun getToken() {
        val result = stsClient.token?.access_token;
        Assert.assertEquals(result, "Bearer");
    }
}