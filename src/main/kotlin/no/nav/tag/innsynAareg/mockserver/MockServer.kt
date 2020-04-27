package no.nav.tag.innsynAareg.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import org.apache.commons.io.IOUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URL
import java.nio.charset.StandardCharsets

@Profile("local")
@Component
class MockServer @Autowired constructor(@Value("\${mock.port}")  val port: Int, @Value("\${sts.stsUrl}") val stsUrl: String, @Value("\${aareg.aaregArbeidsforhold}") val aaregArbeidsforholdUrl: String, @Value("\${aareg.aaregArbeidsgivere}") val aaregArbeidsgiveredUrl: String, @Value("\${yrkeskodeverk.yrkeskodeUrl}") val yrkeskodeUrl: String, @Value("\${pdl.pdlUrl}") val pdlUrl: String,@Value("\${ereg.url}") val eregUrl: String
) {

    init {
        System.out.println("mocking")
        val server = WireMockServer(WireMockConfiguration().port(port).extensions(ResponseTemplateTransformer(true)))
        val aaregArbeidsforholdPath = URL(aaregArbeidsforholdUrl).path;
        mockForPath(server, aaregArbeidsforholdPath, "tomenhetsregisterRespons.json")
        val aaregArbeidsgiverePath = URL(aaregArbeidsgiveredUrl).path;
        mockForPath(server, aaregArbeidsgiverePath, "arbeidsgiveroversiktaareg.json")
        val stsPath = URL(stsUrl).path
        mockForPath(server,stsPath,"STStoken.json")
        val yrkeskodePath = URL(yrkeskodeUrl).path
        mockForPath(server,yrkeskodePath,"yrkeskoder.json")
        val pdlPath = URL(pdlUrl).path
        mockForPath(server,pdlPath,"pdlRespons.json")
        val eregPath1= URL(eregUrl+ "910825518").path
        val eregPath2= URL(eregUrl+ "910825517").path
        mockForPath(server,eregPath1, "enhetsregisteret.json")
        mockForPath(server,eregPath2, "enhetsregisteret.json")

        server.start()
    }

    private fun mockForPath(server: WireMockServer, path: String, responseFile: String) {
        server.stubFor(WireMock.any(WireMock.urlPathMatching("$path.*"))
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil(responseFile))
                ))
    }

    fun hentStringFraFil(filnavn: String):String{
        return IOUtils.toString(MockServer::class.java.classLoader.getResourceAsStream("mock/$filnavn"), StandardCharsets.UTF_8)
    }
}