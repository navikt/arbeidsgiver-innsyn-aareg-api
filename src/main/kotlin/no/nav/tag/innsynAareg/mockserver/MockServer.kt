package no.nav.tag.innsynAareg.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
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

const val ORGNR_HOVEDENHET_UTEN_AAREG_RETTIGHETER = "123456789";
const val ORGNR_UNDERENHET_UTEN_AAREG_RETTIGHETER = "223456789"
const val ALTINN_PROXY_PATH = "/altinn/ekstern/altinn/api/serviceowner/reportees*"

@Profile("local", "labs")
@Component
class MockServer @Autowired constructor(
    @Value("\${mock.port}") val port: Int,
    @Value("\${sts.stsUrl}") val stsUrl: String,
    @Value("\${aareg.aaregArbeidsforhold}") val aaregArbeidsforholdUrl: String,
    @Value("\${aareg.aaregArbeidsgivere}") val aaregArbeidsgivereUrl: String,
    @Value("\${yrkeskodeverk.yrkeskodeUrl}") val yrkeskodeUrl: String,
    @Value("\${pdl.pdlUrl}") val pdlUrl: String,
    @Value("\${ereg.url}") val eregUrl: String,
    @Value("\${altinn.altinnUrl}") val altinnProxyUrl: String
) {

    init {
        println("mocking")
        val server = WireMockServer(WireMockConfiguration().apply {
            port(port)
            extensions(ResponseTemplateTransformer(true))
        })

        mockForPath(server, URL(aaregArbeidsforholdUrl), "tomArbeidsforholdrespons.json")
        mockArbeidsforholdmedJuridiskEnhet(server, URL(aaregArbeidsforholdUrl))
        mockForPath(server, URL(aaregArbeidsgivereUrl), "tomArbeidsgiveroversiktAareg.json")
        mockAntallArbeidsforholdmedJuridiskEnhet(server, URL(aaregArbeidsgivereUrl))
        mockForPath(server, URL(stsUrl), "STStoken.json")
        mockForPath(server, URL(yrkeskodeUrl), "yrkeskoder.json")
        mockForPath(server, URL(pdlUrl), "pdlRespons.json")

        mockForPath(server, URL(eregUrl + "910825518"), "enhetsregisteret.json")
        mockForPath(server, URL(eregUrl + "910825517"), "enhetsregisteret.json")
        mockEregHierarkiHistorikk(server, URL(eregUrl + "910825518"))
        server.stubFor(
            any(urlPathEqualTo(URL(aaregArbeidsforholdUrl).path))
                .withQueryParam("status", equalTo("ALLE"))
                .withHeader("Nav-Arbeidsgiverident", equalTo(ORGNR_UNDERENHET_UTEN_AAREG_RETTIGHETER))
                .withHeader("Nav-Opplysningspliktigident", equalTo(ORGNR_HOVEDENHET_UTEN_AAREG_RETTIGHETER))
                .willReturn(forbidden())
        )
        server.stubFor(
            any(urlPathEqualTo(URL(eregUrl + ORGNR_UNDERENHET_UTEN_AAREG_RETTIGHETER).path))
                .willReturn(notFound())
        )
        server.stubFor(
            any(urlPathEqualTo(URL(eregUrl + ORGNR_HOVEDENHET_UTEN_AAREG_RETTIGHETER).path))
                .willReturn(notFound())
        )

        val altinnPath = URL(altinnProxyUrl).path
        mockOrganisasjoner(server)
        mocktilgangTilSkjemForBedrift(server, altinnPath)
        mockInvalidSSN(server, altinnPath)

        server.start()
    }

    private fun mockForPath(server: WireMockServer, url: URL, responseFile: String) {
        server.stubFor(
            any(urlPathMatching("${url.path}.*"))
                .willReturn(
                    aResponse()
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

    fun mockArbeidsforholdmedJuridiskEnhet(server: WireMockServer, url: URL) {
        server.stubFor(
            get(urlPathEqualTo(url.path))
                .withHeader("Nav-Opplysningspliktigident", matching("983887457|811076112"))
                .withHeader("Nav-Arbeidsgiverident", matching("910825518|811076422"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("arbeidsforholdrespons.json"))
                )
        )
    }

    private fun mockInvalidSSN(server: WireMockServer, altinnPath: String) {
        server.stubFor(
            get(urlPathEqualTo(ALTINN_PROXY_PATH))
                .withQueryParam("subject", notMatching("$FNR_MED_ORGANISASJONER|$FNR_MED_SKJEMATILGANG"))
                .willReturn(
                    aResponse()
                        .withStatusMessage("Invalid socialSecurityNumber")
                        .withStatus(400)
                        .withHeader("Content-Type", "application/octet-stream")
                )
        )
    }

    fun mockHentNavn(server: WireMockServer, path: String) {
        server.stubFor(
            get(urlPathEqualTo(path))
                .withHeader("Nav-Opplysningspliktigident", equalTo("983887457"))
                .withHeader("Nav-Arbeidsgiverident", equalTo("910825518"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("arbeidsforholdrespons.json"))
                )
        )
    }

    fun mockAntallArbeidsforholdmedJuridiskEnhet(server: WireMockServer, url: URL) {
        server.stubFor(
            get(urlPathEqualTo(url.path))
                .withHeader("Nav-Opplysningspliktigident", equalTo("983887457"))
                .withHeader("Nav-Arbeidsgiverident", equalTo("910825518"))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("arbeidsgiveroversiktaareg.json"))
                )
        )
    }

    fun mockOrganisasjoner(server: WireMockServer) {
        server.stubFor(
            get(urlPathMatching(ALTINN_PROXY_PATH))
                .withQueryParam("subject", equalTo(FNR_MED_ORGANISASJONER))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("organisasjoner.json"))
                )
        )
    }

    final fun mocktilgangTilSkjemForBedrift(server: WireMockServer, altinnPath: String) {
        server.stubFor(
            get(urlPathMatching("$ALTINN_PROXY_PATH.*"))
                .withQueryParam("subject", equalTo(FNR_MED_SKJEMATILGANG))
                .withQueryParam("serviceCode", equalTo(SERVICE_CODE))
                .withQueryParam("serviceEdition", equalTo(SERVICE_EDITION))
                .willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("rettigheterTilSkjema.json"))
                )
        )
    }

    final fun mockEregHierarkiHistorikk(server: WireMockServer, url: URL) {
        server.stubFor(
            get(urlPathEqualTo(url.path))
                .withQueryParam("inkluderHistorikk", equalTo("true"))
                .withQueryParam("inkluderHierarki", equalTo("true"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(hentStringFraFil("enhetsregisterethistorikk.json"))
                )
        )
    }
}