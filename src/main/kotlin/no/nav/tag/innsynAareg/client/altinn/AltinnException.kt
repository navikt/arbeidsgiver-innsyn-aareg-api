package no.nav.tag.innsynAareg.client.altinn

class AltinnException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor(message: String?, e: Exception?) : super(message, e)
}