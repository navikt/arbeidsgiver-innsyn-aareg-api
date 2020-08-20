package no.nav.tag.innsynAareg.models

import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold

sealed class ArbeidsforholdOppslagResultat

object IngenRettigheter : ArbeidsforholdOppslagResultat()

data class ArbeidsforholdFunnet(
    val oversiktOverArbeidsForhold: OversiktOverArbeidsForhold
) : ArbeidsforholdOppslagResultat()

