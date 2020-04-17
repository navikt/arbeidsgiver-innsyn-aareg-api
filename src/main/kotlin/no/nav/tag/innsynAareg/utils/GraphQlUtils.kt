package no.nav.tag.innsynAareg.utils

import org.apache.commons.io.Charsets
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.util.StreamUtils
import java.io.IOException

open class GraphQlUtils(val navnQueryResource: Resource){
    @Value("classpath:pdl/hentPerson.navn.graphql")

    @Throws(IOException::class)
    fun resourceAsString(): String {
        val filinnhold = StreamUtils.copyToString(navnQueryResource!!.inputStream, Charsets.UTF_8)
        return filinnhold.replace("\\s+".toRegex(), " ")
    }
}