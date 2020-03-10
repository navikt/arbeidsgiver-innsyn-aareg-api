package no.nav.tag.innsynAareg.utils

import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Import(TokenGeneratorConfiguration::class)
@Profile("local")
class LocalOidcConfiguration {
}