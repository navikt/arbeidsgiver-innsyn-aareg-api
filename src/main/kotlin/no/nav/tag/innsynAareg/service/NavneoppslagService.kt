package no.nav.tag.innsynAareg.service

import no.nav.tag.innsynAareg.client.aareg.dto.ArbeidsForhold
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.client.pdl.PdlBatchClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NavneoppslagService(
    private val pdlBatchClient: PdlBatchClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)!!

    fun settNavn(arbeidsforholdOversikt: OversiktOverArbeidsForhold) {
        arbeidsforholdOversikt
            .arbeidsforholdoversikter
            ?.chunked(size = 100, transform = this::settNavn)
    }

    private fun settNavn(
        arbeidsforholdOversikt: List<ArbeidsForhold>
    ) {
        val arbeidstakerTabell = arbeidsforholdOversikt
            .map { it.arbeidstaker }
            .filter { it.offentligIdent != null }
            .associateBy { it.offentligIdent!! }

        val personer = pdlBatchClient.getBatchFraPdl(arbeidstakerTabell.keys.toList())
            ?.data
            ?.hentPersonBolk
            ?: emptyList()

        for (person in personer) {
            val arbeidstaker = arbeidstakerTabell[person.ident] ?: continue
            if (person.code != "ok") {
                logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn  {}", person.code)
            }

            arbeidstaker.navn = person.person
                ?.navn
                ?.getOrNull(0)
                ?.let {
                    listOfNotNull(it.fornavn, it.mellomNavn, it.etternavn)
                        .joinToString(" ")
                }
        }

        for (arbeidsforhold in arbeidsforholdOversikt) {
            if (arbeidsforhold.arbeidstaker.navn.isNullOrBlank()) {
                logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn, ukjent grunn, antall arbeidsforhold totalt: ${arbeidsforholdOversikt.size}")
                arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn"
            }
        }
    }
}