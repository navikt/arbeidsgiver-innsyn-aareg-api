package no.nav.tag.innsynAareg.client.altinn

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AltinnConfig @Autowired constructor(
    @Value("\${altinn.proxyUrl}") val proxyUrl: String,
    @Value("\${altinn.altinnUrl") val fallBackUrl: String,
    @Value("\${altinn.altinnHeader}") val altinnHeader: String,
    @Value("\${altinn.APIGwHeader}") val APIGwHeader: String
)