package no.nav.tag.innsynAareg.controller
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
class InnlogginsController {
    @GetMapping(value = ["/innlogget"])
    @ResponseBody
    fun erInnlogget(): String? {
        return "ok"
    }
}