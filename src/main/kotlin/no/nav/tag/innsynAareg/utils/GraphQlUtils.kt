package no.nav.tag.innsynAareg.utils

import org.apache.commons.io.Charsets
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.StreamUtils

@Service
class GraphQlUtils (@Value("classpath:pdl/hentPerson.navn.graphql") val navnQueryResource: Resource){
    fun resourceAsString(): String {
        val filinnhold = StreamUtils.copyToString(navnQueryResource.inputStream, Charsets.UTF_8)
        return filinnhold.replace("\\s+".toRegex(), " ")
    }
}