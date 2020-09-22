package no.nav.tag.innsynAareg.config

import no.nav.metrics.MetricsClient
import no.nav.metrics.MetricsConfig
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("dev", "prod")
class MetrikkConfig {
    init {
        val miljø = System.getenv("NAIS_CLUSTER_NAME")
        MetricsClient.enableMetrics(MetricsConfig.resolveNaisConfig().withEnvironment(miljø))
    }
}