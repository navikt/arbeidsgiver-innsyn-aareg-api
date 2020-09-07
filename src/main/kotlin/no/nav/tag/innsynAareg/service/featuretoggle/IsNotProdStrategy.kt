package no.nav.tag.innsynAareg.service.featuretoggle

import no.finn.unleash.strategy.Strategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class IsNotProdStrategy(@Value("\${nais.cluster.name}") val env: String)  : Strategy{

    private val environment: String? = null

    override fun getName(): String? {
        return "isNotProd"
    }

    override fun isEnabled(map: Map<String, String>): Boolean {
        return isProd(env)
    }

    private fun isProd(environment: String): Boolean {
        return "prod-fss".equals(environment, ignoreCase = true)
    }
}