package no.nav.tag.innsynAareg.models

import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon

sealed class AltinnOppslagResultat

data class AltinnOppslagVellykket(val organisasjoner: List<Organisasjon>) : AltinnOppslagResultat()

object AltinnIngenRettigheter : AltinnOppslagResultat()

