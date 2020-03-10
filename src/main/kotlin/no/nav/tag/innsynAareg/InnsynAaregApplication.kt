package no.nav.tag.innsynAareg

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableOIDCTokenValidation(ignore=["org.springframework"])
class InnsynAaregApplication

fun main(args: Array<String>) {
    runApplication<InnsynAaregApplication>(*args)
    val APP_NAME = "srvAG-Arbforhold"
}
