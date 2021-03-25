package no.nav.tag.innsynAareg.service.featuretoggle

import no.nav.tag.innsynAareg.utils.TokenUtils
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.Mockito

class ByUseridStrategyTest {
    @Test
    fun featureErEnabledNårBrukerIListe() {
        Assertions.assertThat(ByUserIdStrategy(userMock("12345678910")).isEnabled(mapOf("user" to "12345678911,12345678910"))).isEqualTo(true)
    }

    @Test
    fun featureErDisabledNårIkkeBrukerIListe() {
        Assertions.assertThat(ByUserIdStrategy(userMock("12345678910")).isEnabled(mapOf("user" to "10987654321"))).isEqualTo(false)
    }

    @Test
    fun skalReturnereFalseHvisParametreErNull() {
        Assertions.assertThat(ByUserIdStrategy(userMock("12345678912")).isEnabled(null)).isEqualTo(false)
    }

    @Test
    fun skalReturnereFalseHvisBrukerIkkeErSatt() {
        Assertions.assertThat(ByUserIdStrategy(userMock("12345678913")).isEnabled(mapOf())).isEqualTo(false)
    }

    private fun userMock(userId: String): TokenUtils {
        val mock = Mockito.mock(TokenUtils::class.java)
        Mockito.`when`(mock.getSubject()).thenReturn(userId)
        return mock
    }
}
