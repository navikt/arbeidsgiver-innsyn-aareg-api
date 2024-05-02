package no.nav.tag.innsynAareg.client.aareg

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.tag.innsynAareg.models.IngenRettigheter
import no.nav.tag.innsynAareg.service.tokenExchange.TokenExchangeClient
import no.nav.tag.innsynAareg.service.tokenExchange.TokenXToken
import no.nav.tag.innsynAareg.utils.AutentisertBruker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus


@RunWith(SpringRunner::class)
@RestClientTest(
    components = [AaregClient::class],
    properties = [
        "aareg.aaregArbeidsforhold=",
        "aareg.aaregArbeidsgivere="
    ]
)
@AutoConfigureWebClient(registerRestTemplate = true)
class AaregClientTest {

    @Autowired
    lateinit var client: AaregClient

    @Autowired
    lateinit var server: MockRestServiceServer

    @MockBean
    lateinit var autentisertBruker: AutentisertBruker

    @MockBean
    lateinit var tokenExchangeClient: TokenExchangeClient

    @MockBean
    lateinit var multiIssuerConfiguration: MultiIssuerConfiguration

    @Before
    fun setUp() {
        Mockito.`when`(autentisertBruker.jwtToken).thenReturn("fake token")
        Mockito.`when`(tokenExchangeClient.exchangeToken(any())).thenReturn(TokenXToken("fake", "fake", "fake" ,999999999))
    }

    @Test
    fun `hentArbeidsforhold returnerer IngenRettigheter ved 403`() {
        server.expect(requestTo(""))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.FORBIDDEN))

        val result = client.hentArbeidsforhold("", "")
        assertThat(result).isEqualTo(IngenRettigheter)
    }
}