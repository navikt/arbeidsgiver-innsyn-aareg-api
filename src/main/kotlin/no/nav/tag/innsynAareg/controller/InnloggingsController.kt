package no.nav.tag.innsynAareg.controller
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tag.innsynAareg.utils.ISSUER
import no.nav.tag.innsynAareg.utils.LEVEL
import org.springframework.http.CacheControl
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@ProtectedWithClaims(issuer = ISSUER, claimMap = [LEVEL])
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