package no.nav.tag.innsynAareg.controller
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer="selvbetjening", claimMap= ["acr=Level4"])
class InnloggingsController {
    @GetMapping(value = ["/innlogget"])
    @ResponseBody
    fun erInnlogget(): String? {
        return "ok"
    }
}