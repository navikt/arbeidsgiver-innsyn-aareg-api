package no.nav.tag.innsynAareg.service.aareg

class AaregException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, e: Exception?) : super(message, e)
}