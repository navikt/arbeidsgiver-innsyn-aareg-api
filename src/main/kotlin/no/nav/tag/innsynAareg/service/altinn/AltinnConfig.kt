package no.nav.tag.innsynAareg.service.altinn

import lombok.Data
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@Data
class AltinnConfig {
    @Value("\${altinn.altinnUrl}")
    lateinit var altinnUrl: String;
    @Value("\${altinn.proxyUrl}")
    lateinit var proxyUrl: String;
    @Value("\${altinn.altinnHeader}")
    lateinit var altinnHeader: String;
    @Value("\${altinn.APIGwHeader}")
    lateinit var APIGwHeader: String;
}