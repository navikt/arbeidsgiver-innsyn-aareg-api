package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Unprotected
class Test {

    @GetMapping(value = ["/test"])
    @ResponseBody
    fun skrivTest(): ResponseEntity<String> {
        return ResponseEntity.ok<String>("hei")
    }

}

