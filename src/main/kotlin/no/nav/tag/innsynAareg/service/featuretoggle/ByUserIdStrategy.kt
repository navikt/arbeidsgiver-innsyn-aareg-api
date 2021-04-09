package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.strategy.Strategy
import no.nav.tag.innsynAareg.utils.AutentisertBruker
import org.springframework.stereotype.Component

@Component
class ByUserIdStrategy(
    private val autentisertBruker: AutentisertBruker
) : Strategy {
    override fun getName() = "byUserId"

    private fun isCurrentUser(userId: String) =
        autentisertBruker.fødselsnummer == userId

    override fun isEnabled(parameters: Map<String, String>?): Boolean {
        return parameters
            ?.get("user")
            ?.split(',')
            ?.any { isCurrentUser(it) }
            ?: false
    }

}
