package no.nav.tag.innsynAareg.client.pdl

import kotlinx.coroutines.runBlocking
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

interface PdlTokenGenerator {
    fun getToken(): String
}

@Component
@Profile("dev", "prod")
class PdlTokenGeneratorAzureAd(
    @Value("\${pdl.tokenClaim}") private val pdlTokenClaim: String,
) : PdlTokenGenerator {

    private val azureExchangeClient = AzureServiceBuilder.buildAzureService(
        enableDefaultProxy = true
    )

    override fun getToken(): String {
        return runBlocking {
            azureExchangeClient.getAccessToken(pdlTokenClaim)
        }
    }
}

@Component
@Profile("labs", "local")
class PdlTokenGeneratorStub: PdlTokenGenerator {
    override fun getToken(): String {
        return "stub-token"
    }
}