package no.nav.tag.innsynAareg

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun api(): OpenAPI {
        return OpenAPI()
            .info(Info().title("Arbeidsgiver Innsyn i AAreg"))
    }
}