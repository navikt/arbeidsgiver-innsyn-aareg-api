package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.enabled=false"])
class InnloggingsControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `tilgangsstyring utføres`() {
        mockMvc
            .perform(get("/innlogget"))
            .andExpect(status().isUnauthorized)
    }
}