package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.springframework.stereotype.Service

@Service
class UnleashService(
    private val unleash: Unleash,
    private val tokenUtil: TokenUtils
) {

    fun hentFeatureToggles(features: List<String>): Map<String, Boolean?>? {
        return features.map { it to isEnabled(it) }.toMap()
    }

    fun isEnabled(feature: String?): Boolean? {
        return unleash.isEnabled(feature, contextMedInnloggetBruker())
    }

    private fun contextMedInnloggetBruker(): UnleashContext? {
        val builder = UnleashContext.builder()
        builder.userId(tokenUtil.autentisertBruker())
        return builder.build()
    }
}