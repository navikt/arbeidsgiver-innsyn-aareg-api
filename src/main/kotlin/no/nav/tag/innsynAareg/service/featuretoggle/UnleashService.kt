package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.tag.innsynAareg.utils.AutentisertBruker
import org.springframework.stereotype.Service

@Service
class UnleashService(
    private val unleash: Unleash,
    private val autentisertBruker: AutentisertBruker
) {

    fun hentFeatureToggles(features: List<String>): Map<String, Boolean> =
        features.map { it to isEnabled(it) }.toMap()

    fun isEnabled(feature: String?) =
        unleash.isEnabled(feature, contextMedInnloggetBruker())

    private fun contextMedInnloggetBruker() =
        UnleashContext.builder()
            .userId(autentisertBruker.fødselsnummer)
            .build()
}