package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
class UnleashService(private val unleash: Unleash, tokenUtils: TokenUtils){

    private val tokenUtil: TokenUtils = tokenUtils

    fun hentFeatureToggles(features: List<String>): Map<String, Boolean?>? {
        return features.map { it to isEnabled(it) }.toMap()
        //return features.fold(HashMap(),it -> it)
        //return features.associateBy { {it}, { it -> isEnabled(it) }  }
        /*return features.stream().collect(Collectors.toMap(
                { feature: String -> feature }
        ) { feature: String -> isEnabled(feature) })*/
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