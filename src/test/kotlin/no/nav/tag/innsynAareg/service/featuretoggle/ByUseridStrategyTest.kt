package no.nav.tag.innsynAareg.service.featuretoggle

import no.nav.tag.innsynAareg.utils.AutentisertBruker
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito

class ByUseridStrategyTest {
    @Test
    fun featureErEnabledNårBrukerIListe() {
        assertThat(
            ByUserIdStrategy(userMock("12345678910"))
                .isEnabled(mapOf("user" to "12345678911,12345678910"))
        ).isTrue
    }

    @Test
    fun featureErDisabledNårIkkeBrukerIListe() {
        assertThat(
            ByUserIdStrategy(userMock("12345678910"))
                .isEnabled(mapOf("user" to "10987654321"))
        ).isFalse
    }

    @Test
    fun skalReturnereFalseHvisParametreErNull() {
        assertThat(
            ByUserIdStrategy(userMock("12345678912"))
                .isEnabled(null)
        ).isFalse
    }

    @Test
    fun skalReturnereFalseHvisBrukerIkkeErSatt() {
        assertThat(
            ByUserIdStrategy(userMock("12345678913"))
                .isEnabled(mapOf())
        ).isFalse
    }

    private fun userMock(userId: String): AutentisertBruker {
        val mock = Mockito.mock(AutentisertBruker::class.java)
        Mockito.`when`(mock.fødselsnummer).thenReturn(userId)
        return mock
    }
}
