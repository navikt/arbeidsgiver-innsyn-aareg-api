package no.nav.tag.innsynAareg.service.altinn

import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Slf4j
@Component
class AltinnConfig @Autowired constructor(@Value("\${altinn.proxyUrl}") val proxyUrl: String,
                                          @Value("\${altinn.altinnHeader}") val altinnHeader: String);