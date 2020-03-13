package no.nav.tag.innsynAareg.service.sts;

import lombok.Data

@Data
class STSControllerTest {
    var access_token: String? = null

    internal var token_type: String? = null
    internal var expires_in: Int = 0
}

