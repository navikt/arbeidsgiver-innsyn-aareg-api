package no.nav.tag.innsynAareg.client

import no.nav.tag.innsynAareg.service.tokenExchange.TokenExchangeClient
import no.nav.tag.innsynAareg.service.tokenExchange.TokenXToken
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("local")
@Component
class TokenExchangeClientStub: TokenExchangeClient {
    override fun exchangeToken(audience: String): TokenXToken {
        return TokenXToken("", "", "", 2)
    }


}