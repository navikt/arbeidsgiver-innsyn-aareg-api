package no.nav.tag.innsynAareg.controller
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tag.innsynAareg.utils.ACR_CLAIM_NEW
import no.nav.tag.innsynAareg.utils.ISSUER
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = ISSUER,
    claimMap = [ACR_CLAIM_NEW],
    combineWithOr = true,
)
class InnloggingsController {
    @GetMapping(value = ["/innlogget"])
    @ResponseBody
    fun erInnlogget(): ResponseEntity<String> {
        val cacheControl = CacheControl.noStore()
        return ResponseEntity.ok()
            .cacheControl(cacheControl)
            .body("ok")
    }
}