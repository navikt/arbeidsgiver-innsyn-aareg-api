package no.nav.tag.innsynAareg.controller

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.enabled=false"])
class InnloggingsControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `returnerer 401 når request ikke er innlogget`() {
        mockMvc
            .perform(get("/innlogget"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `returnerer 200 når request er innlogget`() {
        val jwt = mockMvc.perform(get("/local/jwt")).andReturn().response.contentAsString
        mockMvc
            .perform(
                get("/innlogget")
                    .header(AUTHORIZATION, "Bearer $jwt")
            )
            .andExpect(status().isOk)
    }
}