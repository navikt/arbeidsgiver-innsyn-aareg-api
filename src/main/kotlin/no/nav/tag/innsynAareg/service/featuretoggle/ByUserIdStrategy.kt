package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.strategy.Strategy
import no.nav.tag.innsynAareg.service.InnsynService
import no.nav.tag.innsynAareg.utils.TokenUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ByUserIdStrategy (val tokenUtils: TokenUtils):Strategy{

    private val logger = LoggerFactory.getLogger(ByUserIdStrategy::class.java)!!

    override fun getName(): String {
        return "byUserId";
    }
    override fun isEnabled(parameters: Map<String, String>): Boolean {

       return parameters["user"]?.split(',')?.any { isCurrentUser(it) } ?: false
    }

    private fun isCurrentUser(userId: String):Boolean {
        logger.info("sjekker om {} er lik {}", userId, tokenUtils.autentisertBruker())
       return tokenUtils.autentisertBruker() == userId
    }


}