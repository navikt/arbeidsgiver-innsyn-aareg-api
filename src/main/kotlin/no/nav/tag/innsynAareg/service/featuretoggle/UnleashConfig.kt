package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.FakeUnleash
import no.finn.unleash.Unleash
import no.finn.unleash.util.UnleashConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class UnleashConfig {

    companion object {
        private const val APP_NAME_UNLEASH = "arbeidsgiver-arbeidsforhold-api"
        private const val UNLEASH_API_URL = "https://unleash.nais.io/api/"
    }

    @Bean
    @Profile("dev", "prod")
    fun initializeUnleash(byEnvironmentStrategy: ByEnvironmentStrategy, byUserIdStrategy: ByUserIdStrategy): Unleash? {
        val config: UnleashConfig = UnleashConfig
            .builder()
            .appName(APP_NAME_UNLEASH)
            .instanceId(APP_NAME_UNLEASH)
            .unleashAPI(UNLEASH_API_URL)
            .build()
        return DefaultUnleash(
            config,
            byEnvironmentStrategy,
            byUserIdStrategy
        )
    }

    @Bean
    @Profile("local", "labs")
    fun unleashMock(): Unleash? {
        val fakeUnleash = FakeUnleash()
        fakeUnleash.enableAll()
        return fakeUnleash
    }
}