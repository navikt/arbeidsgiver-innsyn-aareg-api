package no.nav.tag.innsynAareg.service.featuretoggle

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito
import org.springframework.core.env.Environment
import java.util.HashMap
import java.util.Map

class ByEnvironmentStrategyTest {
    @Test
    fun featureIsEnabledWhenEnvironmentInList() {
        assertThat(ByEnvironmentStrategy(environmentMock("local")).isEnabled(Map.of("miljø", "local,dev-fss"))).isEqualTo(true)
    }

    @Test
    fun strategiSkalHandtereFlereProfiler() {
        assertThat(ByEnvironmentStrategy(environmentMock("local", "test")).isEnabled(Map.of("miljø", "local,dev-fss"))).isEqualTo(true)
    }

    @Test
    fun featureIsDisabledWhenEnvironmentNotInList() {
        assertThat(ByEnvironmentStrategy(environmentMock("dev-fss")).isEnabled(Map.of("miljø", "prod-fss"))).isEqualTo(false)
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