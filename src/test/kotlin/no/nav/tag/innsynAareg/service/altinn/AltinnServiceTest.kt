package no.nav.tag.innsynAareg.service.altinn

import no.nav.tag.innsynAareg.models.altinn.Organisasjon
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.util.*

class AltinnServiceTest {
    val restTemplate = Mockito.mock(RestTemplate::class.java)
    val tokenUtils: TokenUtils = Mockito.mock(TokenUtils::class.java)
    val altinnconfigMock = Mockito.mock(AltinnConfig::class.java)
    val altinnService = AltinnService(altinnconfigMock, restTemplate, tokenUtils)

    @Test
    fun hentOrganisasjoner__skal_kalle_altinn_flere_ganger_ved_stor_respons() {
        val refTilListetype = typeReference<List<Organisasjon>>();
        val tomHeader = HttpHeaders();

        Mockito.`when`<ResponseEntity<*>>(restTemplate.exchange(Matchers.any(String::class.java), Matchers.eq(HttpMethod.GET), Matchers.any(HttpEntity::class.java), Matchers.any(ParameterizedTypeReference::class.java)))
                .thenReturn(ResponseEntity.ok(Arrays.asList<Any>(Organisasjon("ba","bla","bla","1231","bla","bla"))))
                .thenReturn(ResponseEntity.ok(emptyList<Any>()))
        altinnService.getFromAltinn(refTilListetype, "http://blabla", 1,HttpEntity(tomHeader));
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Matchers.endsWith("&\$top=1&\$skip=0"), Matchers.eq(HttpMethod.GET), Matchers.any(HttpEntity::class.java), Matchers.any(ParameterizedTypeReference::class.java))
        Mockito.verify(restTemplate, Mockito.times(1)).exchange(Matchers.endsWith("&\$top=1&\$skip=1"), Matchers.eq(HttpMethod.GET), Matchers.any(HttpEntity::class.java), Matchers.any(ParameterizedTypeReference::class.java))
        Mockito.verifyNoMoreInteractions(restTemplate)
    }
}