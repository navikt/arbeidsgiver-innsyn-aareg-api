package no.nav.tag.innsynAareg.client.aareg

import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.tag.innsynAareg.client.sts.STSClient
import no.nav.tag.innsynAareg.client.sts.STStoken
import no.nav.tag.innsynAareg.models.IngenRettigheter
import no.nav.tag.innsynAareg.utils.LoginServiceTokenHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
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

const val aaregArbeidsforholdUrl = ""
const val aaregArbeidsgiverOversiktUrl = ""

@RunWith(SpringRunner::class)
@RestClientTest(
    components = [AaregClient::class],
    properties = [
        "aareg.aaregArbeidsforhold=$aaregArbeidsforholdUrl",
        "aareg.aaregArbeidsgivere=$aaregArbeidsgiverOversiktUrl"
    ]
)
@AutoConfigureWebClient(registerRestTemplate = true)
class AaregClientTest {

    @Autowired
    lateinit var client: AaregClient

    @Autowired
    lateinit var server: MockRestServiceServer

    @MockBean
    lateinit var stsClient: STSClient

    @MockBean
    lateinit var idTokenHolder: LoginServiceTokenHolder

    @MockBean
    lateinit var multiIssuerConfiguration: MultiIssuerConfiguration

    @Before
    fun setUp() {
        Mockito.`when`(stsClient.token).thenReturn(STStoken(""))
        Mockito.`when`(idTokenHolder.idToken).thenReturn("fake token")
    }

    @Test
    fun `hentArbeidsforhold returnerer IngenRettigheter ved 403`() {
        server.expect(requestTo(aaregArbeidsforholdUrl))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.FORBIDDEN))

        val result = client.hentArbeidsforhold("", "")
        assertThat(result).isEqualTo(IngenRettigheter)
    }
}