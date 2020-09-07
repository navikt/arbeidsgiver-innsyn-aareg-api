package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.FakeUnleash
import no.finn.unleash.Unleash
import no.finn.unleash.util.UnleashConfig
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class UnleashConfig ( tokenUtils: TokenUtils){

    private val APP_NAME = "permitteringsskjema-api"
    private val UNLEASH_API_URL = "https://unleash.nais.adeo.no/api/"
    private val tokenUtils: TokenUtils = tokenUtils

    @Bean
    @Profile("dev", "prod")
    fun initializeUnleash( isNotProdStrategy: IsNotProdStrategy, byUserIdStrategy: ByUserIdStrategy): Unleash? {
        val config: UnleashConfig = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME)
                .unleashAPI(UNLEASH_API_URL)
                .build()
        return DefaultUnleash(
                config,
                isNotProdStrategy,
                byUserIdStrategy
        )
    }

    @Bean
    @Profile("local")
    fun unleashMock(): Unleash? {
        val fakeUnleash = FakeUnleash()
        fakeUnleash.enableAll()
        return fakeUnleash
    }
}