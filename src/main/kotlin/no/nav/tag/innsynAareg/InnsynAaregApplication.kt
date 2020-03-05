package no.nav.tag.innsynAareg

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InnsynAaregApplication {
    companion object {
        lateinit var APP_NAME: String
    }
}

fun main(args: Array<String>) {
    runApplication<InnsynAaregApplication>(*args)
    var APP_NAME = "srvAG-Arbforhold"
}
