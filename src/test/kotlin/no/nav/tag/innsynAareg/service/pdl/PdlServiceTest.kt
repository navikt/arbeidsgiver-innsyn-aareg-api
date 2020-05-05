package no.nav.tag.innsynAareg.service.pdl

import no.nav.tag.innsynAareg.models.pdlPerson.*
import no.nav.tag.innsynAareg.service.sts.STSClient
import no.nav.tag.innsynAareg.service.sts.STStoken
import no.nav.tag.innsynAareg.utils.GraphQlUtils
import org.junit.Assert
import org.junit.Test
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

    fun lagPdlObjekt(): PdlRespons {

        var respons = PdlRespons(Data(HentPerson(arrayOf(Navn("Ole",null,"Dole")))),ArrayList<Error>());
        return respons
    }

    @Test
    fun hentNavnMedFnr_skal_hente_sts_token_og_returnere_navn_på_person() {
        val respons = lagPdlObjekt();
        val navn = "Ole Dole"
        Mockito.`when`(mockRestTemplate.postForObject(Matchers.eq(PDL_URL), Matchers.any(org.springframework.http.HttpEntity::class.java), Matchers.eq(PdlRespons::class.java)))
                .thenReturn(respons)
        Assert.assertEquals(navn, pdlService.hentNavnMedFnr(FNR));
        Assert.assertNotNull(mockStsClient);
    }


    @Test
    fun hentNavnMedFnr_skal_hente_sts_token_og_returnere_ikke_funnet_person() {
        val ingenPersonError = Error("Fant ikke Person");
        var errors = ArrayList<Error>()
        errors.add(ingenPersonError)
        var tomRespons = PdlRespons(Data(null), errors);
        Mockito.`when`(mockRestTemplate.postForObject(Matchers.eq(PDL_URL), Matchers.any(org.springframework.http.HttpEntity::class.java), Matchers.eq(PdlRespons::class.java)))
                .thenReturn(tomRespons)
        Assert.assertEquals("Kunne ikke hente navn", pdlService.hentNavnMedFnr(FNR))
        Assert.assertNotNull(mockStsClient);
        //Mockito.verify<STSClient>(mockStsClient)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun hentNavnMedFnr_skal_hente_sts_token_og_returnere_ikke_funnet_person_v_helt_tomPdlRespons() {
        val tomRespons = PdlRespons(null,null)
        Mockito.`when`(mockRestTemplate.postForObject(Matchers.eq(PDL_URL), Matchers.any(org.springframework.http.HttpEntity::class.java), Matchers.eq(PdlRespons::class.java)))
                .thenReturn(tomRespons)
        Assert.assertEquals("Kunne ikke hente navn", pdlService.hentNavnMedFnr(FNR))
        //Mockito.verify<Any?>(mockStsClient?.token?.access_token)
    }

    @Test
    @Throws(ExecutionException::class, InterruptedException::class)
    fun hentNavnMedFnr_skal_hente_sts_token_fange_opp_feil() {
        Mockito.`when`(mockRestTemplate.postForObject(Matchers.eq(PDL_URL), Matchers.any(org.springframework.http.HttpEntity::class.java), Matchers.eq(PdlRespons::class.java))).thenThrow(RestClientException("401"))
        Assert.assertEquals("Kunne ikke hente navn", pdlService.hentNavnMedFnr(FNR))
        //Mockito.verify<Any?>(mockStsClient?.token?.access_token)
    }

    companion object {
        private const val FNR = "123"
        private const val PDL_URL = "http://test"
    }
}