package no.nav.tag.innsynAareg.service.altinn

import no.nav.tag.innsynAareg.models.altinn.AltinnException
import no.nav.tag.innsynAareg.models.altinn.Organisasjon
import no.nav.tag.innsynAareg.models.altinn.Role
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

const val SERVICE_EDITION = "1"
const val SERVICE_CODE = "4936"
const val FNR_MED_SKJEMATILGANG = "01065500791"
const val FNR_MED_ORGANISASJONER = "00000000000"


@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.port=8082"])
class AltinnServiceIntegrationTest {
    @Autowired
    private val altinnService: AltinnService? = null

    @Test
    fun hentOrganisasjoner__skal_fungere_med_gyldig_fnr() {
        //val organisasjoner: List<Organisasjon> = altinnService!!.hentOrganisasjoner(FNR_MED_ORGANISASJONER)
        Assert.assertEquals("heihå", "heihå");

    }

   /* @Test(expected = AltinnException::class)
    fun hentOrganisasjoner__skal_kaste_altinn_exception_hvis_ugyldig_fnr() {
        altinnService!!.hentOrganisasjoner("11111111111")
    }

    @Test
    fun hentRoller__skal_fungere_med_gyldig_fnr_og_orgno() {
        val roller: List<Role> = altinnService!!.hentRoller(FNR_MED_ORGANISASJONER, "000000000")
        val testRolle = roller.find { it.RoleDescription == "Import, processing, production and/or sales of primary products and other foodstuff"}
        Assert.assertEquals("Import, processing, production and/or sales of primary products and other foodstuff", testRolle!!.RoleDescription)
    }

    @Test
    fun henttilgangTilSkjemForBedrift() {
        val organisasjoner: List<Organisasjon> = altinnService!!.hentOrganisasjonerBasertPaRettigheter(FNR_MED_SKJEMATILGANG, SERVICE_CODE, SERVICE_EDITION)
        Assert.assertEquals(5, organisasjoner.size);
    }

    */
}