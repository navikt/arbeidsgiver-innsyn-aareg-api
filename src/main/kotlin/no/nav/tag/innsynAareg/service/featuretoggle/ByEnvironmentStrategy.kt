package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.strategy.Strategy
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.util.*

@Component
class ByEnvironmentStrategy( currentEnv: Environment) : Strategy {

    val MILJOER = Arrays.asList("dev-fss", "prod-fss")
    val currentEnv = currentEnv.activeProfiles.find { MILJOER.contains(it)} ?: "dev-fss"

    override fun getName(): String {
       return "byEnvironment"
    }

    override fun isEnabled(parameters: Map<String, String>?): Boolean {

        return parameters?.get("miljø")?.split(',')?.any { isCurrentEnv(it) } ?: false
    }

    fun isCurrentEnv(env: String):Boolean {
        return currentEnv == env
    }
}
