package no.nav.tag.innsynAareg.service.featuretoggle

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.springframework.core.env.Environment

class ByEnvironmentStrategyTest {
    @Test
    fun featureIsEnabledMedMiljøILista() {
        assertThat(ByEnvironmentStrategy(environmentMock("local")).isEnabled(mapOf("miljø" to "local,dev-fss"))).isEqualTo(true)
    }

    @Test
    fun strategiSkalHandtereFlereProfiler() {
        assertThat(ByEnvironmentStrategy(environmentMock("local", "test")).isEnabled(mapOf("miljø" to "local,dev-fss"))).isEqualTo(true)
    }

    @Test
    fun featureIsDisabledMedMiljøNull() {
        assertThat(ByEnvironmentStrategy(environmentMock("dev-fss")).isEnabled(mapOf("miljø" to "prod-fss"))).isEqualTo(false)
    }

    @Test
    fun skalReturnereFalseHvisParametreErNull() {
        assertThat(ByEnvironmentStrategy(environmentMock("dev-fss")).isEnabled(null)).isEqualTo(false)
    }

    @Test
    fun skalReturnereFalseHvisMiljøIkkeErSatt() {
        assertThat(ByEnvironmentStrategy(environmentMock("dev-fss")).isEnabled(HashMap())).isEqualTo(false)
    }

    private fun environmentMock(vararg profiles: String): Environment {
        val mock = Mockito.mock(Environment::class.java)
        Mockito.`when`(mock.activeProfiles).thenReturn(profiles)
        return mock
    }
}