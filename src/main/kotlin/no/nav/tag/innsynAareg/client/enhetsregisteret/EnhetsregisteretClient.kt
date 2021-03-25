package no.nav.tag.innsynAareg.client.enhetsregisteret

import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.OrganisasjonFraEreg
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

@Service
class EnhetsregisteretClient(
    private val restTemplate: RestTemplate
) {
    val logger = LoggerFactory.getLogger(EnhetsregisteretClient::class.java)!!

    @Value("\${ereg.url}")
    private lateinit var eregUrl: String

    private val requestEntity: HttpEntity<String> = HttpEntity(HttpHeaders())

    fun hentOrganisasjonFraEnhetsregisteret(
        orgnr: String,
        inkluderHistorikk: Boolean
    ): OrganisasjonFraEreg? {
        return try {
            val eregurMedParam = "$eregUrl$orgnr?inkluderHistorikk=$inkluderHistorikk&inkluderHierarki=true"
            val response = restTemplate.exchange(
                eregurMedParam,
                HttpMethod.GET,
                requestEntity,
                OrganisasjonFraEreg::class.java
            )
            logger.info("sjekker om organisasjon inngår i orgledd")
            response.body
        } catch (e: Exception) {
            logger.error("Feil ved oppslag mot EnhetsRegisteret:", e)
            null
        }
    }


    fun OrganisasjonFraEreg.gyldigTilOgMed(): LocalDate {
        val tom = gyldighetsperiode?.tom
        return if (tom.isNullOrBlank()) {
           LocalDate.MAX
        } else {
            LocalDate.parse(tom)
        }
    }

    fun finnTidligereVirksomheter(juridiskEnhet: String, idtoken: String): List<Organisasjon> {
        val driverVirksomhetene = hentOrganisasjonFraEnhetsregisteret(
            juridiskEnhet,
            true
        )
            ?.driverVirksomheter
            ?.filter { it.gyldighetsperiode != null }
            ?: emptyList()

        val now = LocalDate.now()

        val (aktiveVirksomheter, virksomheterSomHarVærtInaktiv) = driverVirksomhetene
            .partition { virksomhet ->
                now.isBefore(virksomhet.gyldigTilOgMed())
            }

        val aktiveVirksomhetsnummer = aktiveVirksomheter
            .map { it.organisasjonsnummer }
            .toSet()

        return virksomheterSomHarVærtInaktiv
            .filterNot { organisasjon ->
                aktiveVirksomhetsnummer.contains(organisasjon.organisasjonsnummer)
            }
            .map {
                Organisasjon(
                    Name = it.navn?.redigertnavn,
                    ParentOrganizationNumber = juridiskEnhet,
                    OrganizationNumber = it.organisasjonsnummer
                )
            }
            .also {
                logger.info(
                    "hent tidligere virksomheter gitt juridiskEnhet gir liste med organisasjoner med lengde",
                    it.size
                )
            }
    }

}