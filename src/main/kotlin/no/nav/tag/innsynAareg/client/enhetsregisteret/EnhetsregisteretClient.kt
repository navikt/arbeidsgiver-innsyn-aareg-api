package no.nav.tag.innsynAareg.client.enhetsregisteret

import no.nav.tag.innsynAareg.client.enhetsregisteret.dto.OrganisasjonFraEreg
import no.nav.tag.innsynAareg.client.altinn.dto.Organisasjon
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

@Service
class EnhetsregisteretClient(private val restTemplate: RestTemplate) {
    val logger = LoggerFactory.getLogger(EnhetsregisteretClient::class.java)!!

    @Value("\${ereg.url}")
    private val eregUrl: String? = null

    private val requestEntity: HttpEntity<String>

    private fun getRequestEntity(): HttpEntity<String> {
        val headers = HttpHeaders()
        return HttpEntity(headers)
    }

    fun hentOrganisasjonFraEnhetsregisteret(orgnr: String, inkluderHistorikk:Boolean): OrganisasjonFraEreg? {
        try {
            val eregurMedParam = "$eregUrl$orgnr?inkluderHistorikk=$inkluderHistorikk&inkluderHierarki=true"
            val response: ResponseEntity<OrganisasjonFraEreg> = restTemplate.exchange(
                eregurMedParam,
                HttpMethod.GET,
                requestEntity,
                OrganisasjonFraEreg::class.java
            )
            logger.info("respons fra enhetsregisteret: {}", response.body?.navn?.redigertnavn ?: "fant ingen ting")
            logger.info("sjekker om organisasjon inngår i orgledd for organisasjon: $orgnr")
            return response.body
        } catch (e: Exception) {
            logger.error("Feil ved oppslag mot EnhetsRegisteret: orgnr: $orgnr", e)
        }
        return null
    }

    fun finnTidligereVirksomheter(juridiskEnhet: String): List<Organisasjon>? {
        val organisasjonsInfoFraEreg = hentOrganisasjonFraEnhetsregisteret(juridiskEnhet,true);
        if (organisasjonsInfoFraEreg != null && !organisasjonsInfoFraEreg.driverVirksomheter.isNullOrEmpty()) {
           val inaktiveEregOrgs = organisasjonsInfoFraEreg.driverVirksomheter.filter { it.gyldighetsperiode!=null && sjekkOmDatoErFørDagensDato(it.gyldighetsperiode.tom)  }
           val aktiveEregOrgs = organisasjonsInfoFraEreg.driverVirksomheter.filter { it.gyldighetsperiode!=null && !sjekkOmDatoErFørDagensDato(it.gyldighetsperiode.tom)  }
           val komplementTilAktiveOrgs = inaktiveEregOrgs.filterNot { organisasjon -> aktiveEregOrgs.any { it.organisasjonsnummer == organisasjon.organisasjonsnummer }}
           val komplementPaaAltinnFormat = mapFraOrganisasjonFraEregTilAltinn(komplementTilAktiveOrgs, juridiskEnhet);
           logger.info("hent tidligere virksomheter gitt juridiskEnhet: {}. gir denne lista:\n{}", juridiskEnhet, komplementPaaAltinnFormat.joinToString(" \n") { it.toString() })
           return komplementPaaAltinnFormat
        }
        return null
    }

    fun sjekkOmDatoErFørDagensDato (dato :String?):Boolean{
        return if(dato.isNullOrBlank()) false
        else
            LocalDate.parse(dato).isBefore(LocalDate.now())
    }

    fun mapFraOrganisasjonFraEregTilAltinn(virksomheter: List<OrganisasjonFraEreg>, juridiskEnhet: String): List<Organisasjon> {
        return virksomheter.map {
            Organisasjon(
                    Name = it.navn?.redigertnavn,
                    ParentOrganizationNumber = juridiskEnhet,
                    OrganizationNumber = it.organisasjonsnummer
            )
        }
    }

    init {
        requestEntity = getRequestEntity()
    }
}