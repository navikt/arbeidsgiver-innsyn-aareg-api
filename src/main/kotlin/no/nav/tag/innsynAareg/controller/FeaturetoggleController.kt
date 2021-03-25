package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.core.api.Unprotected
import no.nav.tag.innsynAareg.service.featuretoggle.UnleashService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class FeaturetoggleController(val unleashService: UnleashService) {
    @GetMapping(value = ["/feature"])
    fun feature(
        @RequestParam("feature") features: List<String>
    ): ResponseEntity<Map<String, Boolean?>> {
        return ResponseEntity.status(HttpStatus.OK).body(unleashService.hentFeatureToggles(features))
    }
}
