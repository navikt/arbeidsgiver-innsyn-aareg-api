package no.nav.tag.innsynAareg

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableJwtTokenValidation(
    ignore = ["org.springframework", "org.springdoc"]
)
class InnsynAaregApplication

fun main(args: Array<String>) {
    runApplication<InnsynAaregApplication>(*args)
}