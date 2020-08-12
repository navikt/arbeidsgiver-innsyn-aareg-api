package no.nav.tag.innsynAareg.client.aareg

class AaregException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, e: Exception?) : super(message, e)
}