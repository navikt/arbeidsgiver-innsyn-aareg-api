package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.strategy.Strategy
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.springframework.stereotype.Component

@Component
class ByUserIdStrategy(val tokenUtils: TokenUtils) : Strategy {

    override fun getName(): String {
        return "byUserId";
    }

    override fun isEnabled(parameters: Map<String, String>?): Boolean {
        return parameters?.get("user")?.split(',')?.any { isCurrentUser(it) } ?: false
    }

    private fun isCurrentUser(userId: String): Boolean {
        return tokenUtils.getSubject() == userId
    }
}
