package no.nav.tag.innsynAareg.mockserver

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer
import com.github.tomakehurst.wiremock.matching.UrlPattern
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

fun WireMockServer.stubForGet(urlPattern: UrlPattern, builder: MappingBuilder.() -> Unit) {
    stubFor(get(urlPattern).apply(builder))
}

fun WireMockServer.stubForAny(urlPattern: UrlPattern, builder: MappingBuilder.() -> Unit) {
    stubFor(any(urlPattern).apply(builder))
}

fun MappingBuilder.willReturnJson(body: String) {
    willReturn(
        aResponse().apply {
            withHeader("Content-Type", "application/json")
            withBody(body)
        }
    )
}

@Profile("local", "labs")
@Component
class MockServer @Autowired constructor(
    @Value("\${mock.port}") private val port: Int,
    @Value("\${sts.stsUrl}") private val stsUrl: String,
    @Value("\${aareg.aaregArbeidsforhold}") private val aaregArbeidsforholdUrl: String,
    @Value("\${aareg.aaregArbeidsgivere}") private val aaregArbeidsgivereUrl: String,
    @Value("\${yrkeskodeverk.yrkeskodeUrl}") private val yrkeskodeUrl: String,
    @Value("\${pdl.pdlUrl}") private val pdlUrl: String,
    @Value("\${ereg.url}") private val eregUrl: String
) {

    init {
        println("mocking")

        val config = WireMockConfiguration().apply {
            port(port)
            extensions(ResponseTemplateTransformer(true))
        }

        WireMockServer(config).apply {
            setup()
        }
    }

    private final fun WireMockServer.setup() {

        stubForAny(urlPathMatching("${URL(aaregArbeidsforholdUrl).path}.*")) {
            willReturnJson(hentStringFraFil("tomArbeidsforholdrespons.json"))
        }

        stubForGet(urlPathEqualTo(URL(aaregArbeidsforholdUrl).path)) {
            withHeader("Nav-Opplysningspliktigident", matching("983887457|811076112"))
            withHeader("Nav-Arbeidsgiverident", matching("910825518|811076422"))
            willReturnJson(hentStringFraFil("arbeidsforholdrespons.json"))
        }

        stubForAny(urlPathMatching("${URL(aaregArbeidsgivereUrl).path}.*")) {
            willReturnJson(hentStringFraFil("tomArbeidsgiveroversiktAareg.json"))
        }

        stubForGet(urlPathEqualTo(URL(aaregArbeidsgivereUrl).path)) {
            withHeader("Nav-Opplysningspliktigident", equalTo("983887457"))
            withHeader("Nav-Arbeidsgiverident", equalTo("910825518"))
            willReturnJson(hentStringFraFil("arbeidsgiveroversiktaareg.json"))
        }

        stubForAny(urlPathMatching("${URL(stsUrl).path}.*")) {
            willReturnJson(hentStringFraFil("STStoken.json"))
        }

        stubForAny(urlPathMatching("${URL(yrkeskodeUrl).path}.*")) {
            willReturnJson(hentStringFraFil("yrkeskoder.json"))
        }

        stubForAny(urlPathMatching("${URL(pdlUrl).path}.*")) {
            willReturnJson(hentStringFraFil("pdlRespons.json"))
        }

        stubForAny(urlPathMatching("${URL(eregUrl + "910825518").path}.*")) {
            willReturnJson(hentStringFraFil("enhetsregisteret.json"))
        }

        stubForAny(urlPathMatching("${URL(eregUrl + "910825517").path}.*")) {
            willReturnJson(hentStringFraFil("enhetsregisteret.json"))
        }

        stubForGet(urlPathEqualTo(URL(eregUrl + "910825518").path)) {
            withQueryParam("inkluderHistorikk", equalTo("true"))
            withQueryParam("inkluderHierarki", equalTo("true"))
            willReturnJson(hentStringFraFil("enhetsregisterethistorikk.json"))
        }

        stubForAny(urlPathEqualTo(URL(aaregArbeidsforholdUrl).path)) {
            withQueryParam("status", equalTo("ALLE"))
            withHeader("Nav-Arbeidsgiverident", equalTo(ORGNR_UNDERENHET_UTEN_AAREG_RETTIGHETER))
            withHeader("Nav-Opplysningspliktigident", equalTo(ORGNR_HOVEDENHET_UTEN_AAREG_RETTIGHETER))
            willReturn(forbidden())
        }

        stubForAny(urlPathEqualTo(URL(eregUrl + ORGNR_UNDERENHET_UTEN_AAREG_RETTIGHETER).path)) {
            willReturn(notFound())
        }

        stubForAny(urlPathEqualTo(URL(eregUrl + ORGNR_HOVEDENHET_UTEN_AAREG_RETTIGHETER).path)) {
            willReturn(notFound())
        }

        stubForGet(urlPathMatching(ALTINN_PROXY_PATH)) {
            withQueryParam("subject", equalTo(FNR_MED_ORGANISASJONER))
            willReturnJson(hentStringFraFil("organisasjoner.json"))

        }

        stubForGet(urlPathMatching("$ALTINN_PROXY_PATH.*")) {
            withQueryParam("subject", equalTo(FNR_MED_SKJEMATILGANG))
            withQueryParam("serviceCode", equalTo(SERVICE_CODE))
            withQueryParam("serviceEdition", equalTo(SERVICE_EDITION))
            willReturnJson(hentStringFraFil("rettigheterTilSkjema.json"))
        }

        stubForGet(urlPathEqualTo(ALTINN_PROXY_PATH)) {
            withQueryParam("subject", notMatching("$FNR_MED_ORGANISASJONER|$FNR_MED_SKJEMATILGANG"))
            willReturn(
                aResponse()
                    .withStatusMessage("Invalid socialSecurityNumber")
                    .withStatus(400)
                    .withHeader("Content-Type", "application/octet-stream")
            )
        }

        start()
    }

    private final fun hentStringFraFil(filnavn: String): String {
        return IOUtils.toString(
            MockServer::class.java.classLoader.getResourceAsStream("mock/$filnavn"),
            StandardCharsets.UTF_8
        )
    }

    private final fun WireMockServer.mockHentNavn(path: String) {
        stubForGet(urlPathEqualTo(path)) {
            withHeader("Nav-Opplysningspliktigident", equalTo("983887457"))
            withHeader("Nav-Arbeidsgiverident", equalTo("910825518"))
            willReturnJson(hentStringFraFil("arbeidsforholdrespons.json"))
        }
    }
}