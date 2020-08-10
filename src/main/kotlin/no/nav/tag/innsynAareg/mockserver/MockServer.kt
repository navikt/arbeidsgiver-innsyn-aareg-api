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

const val SERVICE_EDITION = "1"
const val SERVICE_CODE = "4936"
const val FNR_MED_SKJEMATILGANG = "01065500791"
const val FNR_MED_ORGANISASJONER = "00000000000"

@Profile("local")
@Component
class MockServer @Autowired constructor(
    @Value("\${mock.port}") val port: Int,
    @Value("\${sts.stsUrl}") val stsUrl: String,
    @Value("\${aareg.aaregArbeidsforhold}") val aaregArbeidsforholdUrl: String,
    @Value("\${aareg.aaregArbeidsgivere}") val aaregArbeidsgiveredUrl: String,
    @Value("\${yrkeskodeverk.yrkeskodeUrl}") val yrkeskodeUrl: String,
    @Value("\${pdl.pdlUrl}") val pdlUrl: String,
    @Value("\${ereg.url}") val eregUrl: String,
    @Value("\${altinn.altinnUrl}") val altinnProxyUrl: String
) {

    init {
        println("mocking")
        val server = WireMockServer(WireMockConfiguration().port(port).extensions(ResponseTemplateTransformer(true)))
        val aaregArbeidsforholdPath = URL(aaregArbeidsforholdUrl).path
        mockForPath(server, aaregArbeidsforholdPath, "tomArbeidsforholdrespons.json")
        mockArbeidsforholdmedJuridiskEnhet(server, aaregArbeidsforholdPath)
        val aaregArbeidsgiverePath = URL(aaregArbeidsgiveredUrl).path
        mockForPath(server, aaregArbeidsgiverePath, "tomArbeidsgiveroversiktAareg.json")
        mockAntallArbeidsforholdmedJuridiskEnhet(server, aaregArbeidsgiverePath)
        val stsPath = URL(stsUrl).path
        mockForPath(server, stsPath,"STStoken.json")
        val yrkeskodePath = URL(yrkeskodeUrl).path
        mockForPath(server, yrkeskodePath,"yrkeskoder.json")
        val pdlPath = URL(pdlUrl).path
        mockForPath(server,pdlPath,"pdlRespons.json")
        val eregPath1= URL(eregUrl+ "910825518").path
        val eregPath2= URL(eregUrl+ "910825517").path
        val altinnPath = URL(altinnProxyUrl).path
        mockForPath(server, eregPath1, "enhetsregisteret.json")
        mockForPath(server, eregPath2, "enhetsregisteret.json")
        mocktilgangTilSkjemForBedrift(server, altinnPath)
        mockOrganisasjoner(server, altinnPath)
        mockInvalidSSN(server, altinnPath)

        server.start()
    }

    private fun mockForPath(server: WireMockServer, path: String, responseFile: String) {
        server.stubFor(
            WireMock.any(WireMock.urlPathMatching("$path.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil(responseFile))
                )
        )
    }

    fun hentStringFraFil(filnavn: String): String {
        return IOUtils.toString(
            MockServer::class.java.classLoader.getResourceAsStream("mock/$filnavn"),
            StandardCharsets.UTF_8
        )
    }

    fun mockArbeidsforholdmedJuridiskEnhet(server: WireMockServer, path: String) {
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(path))
                .withHeader("Nav-Opplysningspliktigident", WireMock.equalTo("983887457"))
                .withHeader("Nav-Arbeidsgiverident", WireMock.equalTo("910825518"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("arbeidsforholdrespons.json"))
                )
        )
    }

    private fun mockInvalidSSN(server: WireMockServer, altinnPath: String) {
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(altinnPath + "reportees/"))
                .withQueryParam("subject", WireMock.notMatching("$FNR_MED_ORGANISASJONER|$FNR_MED_SKJEMATILGANG"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatusMessage("Invalid socialSecurityNumber")
                        .withStatus(400)
                        .withHeader("Content-Type", "application/octet-stream")
                )
        )
    }

    fun mockHentNavn( server: WireMockServer, path: String) {
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(path))
                .withHeader("Nav-Opplysningspliktigident", WireMock.equalTo("983887457"))
                .withHeader("Nav-Arbeidsgiverident", WireMock.equalTo("910825518"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("arbeidsforholdrespons.json"))
                )
        )
    }

    fun mockAntallArbeidsforholdmedJuridiskEnhet(server: WireMockServer, path: String) {
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(path))
                .withHeader("Nav-Opplysningspliktigident", WireMock.equalTo("983887457"))
                .withHeader("Nav-Arbeidsgiverident", WireMock.equalTo("910825518"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("arbeidsgiveroversiktaareg.json"))
                )
        )
    }

    fun mockOrganisasjoner(server: WireMockServer, altinnPath: String) {
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(altinnPath + "reportees/"))
                .withQueryParam("subject", WireMock.equalTo(FNR_MED_ORGANISASJONER))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("organisasjoner.json"))
                )
        )
    }

    final fun mocktilgangTilSkjemForBedrift(server: WireMockServer, altinnPath: String) {
        server.stubFor(
            WireMock.get(WireMock.urlPathEqualTo(altinnPath + "reportees/"))
                .withQueryParam("subject", WireMock.equalTo(FNR_MED_SKJEMATILGANG))
                .withQueryParam("serviceCode", WireMock.equalTo(SERVICE_CODE))
                .withQueryParam("serviceEdition", WireMock.equalTo(SERVICE_EDITION))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("rettigheterTilSkjema.json"))
                )
        )
    }
}