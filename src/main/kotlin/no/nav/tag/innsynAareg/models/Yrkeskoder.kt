package no.nav.tag.innsynAareg.models

import org.slf4j.LoggerFactory

class Yrkeskoder(
    private val betydninger: Map<String, String>
) {
    constructor() : this(hashMapOf())

    private val logger = LoggerFactory.getLogger(Yrkeskoder::class.java)!!

    fun betydningPåYrke(yrkeskodenokkel: String): String =
        betydninger[yrkeskodenokkel] ?: run {
            logger.error("Fant ikke betydning for yrkeskode '{}'", yrkeskodenokkel)
            "Fant ikke yrkesbeskrivelse"
        }
}
