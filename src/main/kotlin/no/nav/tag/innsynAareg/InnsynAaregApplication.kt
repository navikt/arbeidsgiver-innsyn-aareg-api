package no.nav.tag.innsynAareg

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InnsynAaregApplication

fun main(args: Array<String>) {
    runApplication<InnsynAaregApplication>(*args)
    val APP_NAME = "srvAG-Arbforhold"
}
