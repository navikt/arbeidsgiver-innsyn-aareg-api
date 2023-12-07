package no.nav.tag.innsynAareg.controller
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.client.enhetsregisteret.EnhetsregisteretClient
import no.nav.tag.innsynAareg.utils.ACR_CLAIM_NEW
import no.nav.tag.innsynAareg.utils.ISSUER
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(
    issuer = ISSUER,
    claimMap = [ACR_CLAIM_NEW],
    combineWithOr = true,
)
class EnhetsregisteretController(
    val enhetsregisteretClient: EnhetsregisteretClient
) {
    @GetMapping(value = ["/tidligere-virksomheter"])
    fun hentTidligerVirksomheter(
        @RequestHeader("jurenhet") juridiskEnhetOrgnr: String,
    ): ResponseEntity<List<Organisasjon>> {
        val result = enhetsregisteretClient.finnTidligereVirksomheter(juridiskEnhetOrgnr)
        return ResponseEntity.ok(result)
    }
}