package no.nav.tag.innsynAareg.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class Test {

    @GetMapping(value = ["/test"])
    @ResponseBody
    fun skrivTest(): ResponseEntity<String> {

        return ResponseEntity.ok<String>("heihHEHEI")
    }

}

