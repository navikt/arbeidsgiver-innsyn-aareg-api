package no.nav.tag.innsynAareg.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.tag.innsynAareg.client.aareg.dto.ArbeidsForhold
import no.nav.tag.innsynAareg.client.aareg.dto.OversiktOverArbeidsForhold
import no.nav.tag.innsynAareg.client.pdl.PdlBatchClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class NavneoppslagService(
    registry: MeterRegistry,
    private val pdlBatchClient: PdlBatchClient,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)!!
    private val pdlManglerPersonnavn = registry.counter("pdl.mangler.personnavn")

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
            .groupBy { it.offentligIdent!! }

        val personer = pdlBatchClient.getBatchFraPdl(arbeidstakerTabell.keys.toList())
            ?.data
            ?.hentPersonBolk
            ?: emptyList()

        for (person in personer) {
            if (person.code != "ok") {
                logger.error("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn {}", person.code)
            }

            for (arbeidstaker in arbeidstakerTabell.getOrDefault(person.ident, emptyList())) {
                arbeidstaker.navn = person.person
                    ?.navn
                    ?.getOrNull(0)
                    ?.let {
                        listOfNotNull(it.fornavn, it.mellomNavn, it.etternavn).joinToString(" ")
                    }
            }
        }

        for (arbeidsforhold in arbeidsforholdOversikt) {
            if (arbeidsforhold.arbeidstaker.navn.isNullOrBlank()) {
                pdlManglerPersonnavn.increment()
                logger.info("AG-ARBEIDSFORHOLD PDL ERROR fant ikke navn, ukjent grunn, antall arbeidsforhold totalt: ${arbeidsforholdOversikt.size}")
                arbeidsforhold.arbeidstaker.navn = "Kunne ikke hente navn"
            }
        }
    }
}