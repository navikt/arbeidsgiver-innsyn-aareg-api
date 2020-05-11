package no.nav.tag.innsynAareg.service.altinn

import lombok.Data
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@Data
@ConfigurationProperties(prefix = "altinn")
class AltinnConfig {
    val altinnHeader: String? = null
    val altinnurl: String? = null
    val APIGwHeader: String? = null
    val proxyUrl: String? = null
}