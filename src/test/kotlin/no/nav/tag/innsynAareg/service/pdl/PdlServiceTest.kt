package no.nav.tag.innsynAareg.service.pdl

import no.nav.tag.innsynAareg.models.pdlPerson.*
import no.nav.tag.innsynAareg.service.sts.STSClient
import no.nav.tag.innsynAareg.utils.GraphQlUtils
import org.apache.http.HttpEntity
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Matchers
import org.mockito.Mockito
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.concurrent.ExecutionException


class PdlServiceTest {


    val mockStsClient = Mockito.mock(STSClient::class.java)
    val mockRestTemplate = Mockito.mock(RestTemplate::class.java)
    val graphQlUtils = Mockito.mock(GraphQlUtils::class.java)
    val pdlService = PdlService(mockRestTemplate,mockStsClient,graphQlUtils,"http://test")
    //val mockRespons = lagPdlObjekt();

    /*@SpringBootTest
    @TestPropertySource(properties = ["mock.port=8082"])
    @RunWith(SpringRunner::class)
    class PdlServiceTest {
        var respons: PdlRespons? = null
        @Mock
        private val restTemplate: RestTemplate? = null
        @InjectMocks
        private val pdlService: PdlService? = null
        @Mock
        var stsClient: STSClient? = null
        @Mock
        private val graphQlUtils: GraphQlUtils? = null

        @Before
        fun setUp() {
            Mockito.`when`(stsClient?.token).thenReturn(STStoken())
            respons = PdlRespons()
            lagPdlObjekt()
        }

        fun lagPdlObjekt() {
            respons?.data = Data()
            respons?.data?.hentPerson = HentPerson()
            val testNavn = Navn()
            testNavn.fornavn = "Ole"
            testNavn.etternavn = "Dole"
            respons?.data?.hentPerson?.navn = arrayOf(testNavn);
        }

        @Test
        @Throws(ExecutionException::class, InterruptedException::class)
        fun hentNavnMedFnr_skal_hente_sts_token_og_returnere_navn_på_person() {
            val navn = "Ole Dole"
            Mockito.`when`(restTemplate?.postForObject(ArgumentMatchers.eq(PDL_URL), ArgumentMatchers.any(HttpEntity::class.java), ArgumentMatchers.eq(PdlRespons::class.java)))
                    .thenReturn(respons)
            Assert.assertEquals(navn, pdlService!!.hentNavnMedFnr(FNR))
            Mockito.verify<Any?>(stsClient?.token?.access_token)
        }
        */

    fun lagPdlObjekt(): PdlRespons {
        var respons = PdlRespons()
        var testNavn = Navn()
        testNavn.fornavn = "Ole"
        testNavn.etternavn = "Dole"
        respons.data = Data()
        respons.data!!.hentPerson = HentPerson()
        respons.data!!.hentPerson!!.navn = arrayOf(testNavn)
        return respons
    }

    @Test
    fun hentNavnMedFnr_skal_hente_sts_token_og_returnere_navn_på_person() {
        val respons = lagPdlObjekt();
        val navn = "Ole Dole"
        Mockito.`when`(mockRestTemplate.postForObject(Matchers.eq(PDL_URL), Matchers.any(org.springframework.http.HttpEntity::class.java), Matchers.eq(PdlRespons::class.java)))
                .thenReturn(respons)
        Assert.assertEquals(navn, pdlService.hentNavnMedFnr(FNR));
       // Mockito.verify<Any?>(mockStsClient.token?.access_token)
    }


    @Test
    fun hentNavnMedFnr_skal_hente_sts_token_og_returnere_ikke_funnet_person() {
        var tomRespons = PdlRespons()
        val ingenPersonError = Error("Fant ikke Person");
        tomRespons.data = Data()
        tomRespons.data!!.hentPerson = null
        tomRespons.errors = ArrayList<Error>()
        tomRespons.errors!!.add(ingenPersonError)
        Mockito.`when`(mockRestTemplate.postForObject(Matchers.eq(PDL_URL), Matchers.any(org.springframework.http.HttpEntity::class.java), Matchers.eq(PdlRespons::class.java)))
                .thenReturn(tomRespons)
        Assert.assertEquals("Kunne ikke hente navn", pdlService.hentNavnMedFnr(FNR))
       // Mockito.verify<Any?>(mockStsClient?.token?.access_token)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun hentNavnMedFnr_skal_hente_sts_token_og_returnere_ikke_funnet_person_v_helt_tomPdlRespons() {
        val tomRespons = PdlRespons()
        Mockito.`when`(mockRestTemplate.postForObject(Matchers.eq(PDL_URL), Matchers.any(org.springframework.http.HttpEntity::class.java), Matchers.eq(PdlRespons::class.java)))
                .thenReturn(tomRespons)
        Assert.assertEquals("Kunne ikke hente navn", pdlService.hentNavnMedFnr(FNR))
        //Mockito.verify<Any?>(mockStsClient?.token?.access_token)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun hentNavnMedFnr_skal_hente_sts_token_fange_opp_feil() {
        Mockito.`when`(mockRestTemplate.postForObject(Matchers.eq(PDL_URL), Matchers.any(org.springframework.http.HttpEntity::class.java), Matchers.eq(PdlRespons::class.java))).thenThrow(RestClientException("401"))
        Assert.assertEquals("Kunne ikke hente navn", pdlService!!.hentNavnMedFnr(FNR))
        //Mockito.verify<Any?>(mockStsClient?.token?.access_token)
    }

    companion object {
        private const val FNR = "123"
        private const val PDL_URL = "http://test"
    }
}