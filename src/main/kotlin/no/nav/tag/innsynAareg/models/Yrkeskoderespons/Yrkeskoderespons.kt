package no.nav.tag.innsynAareg.models.Yrkeskoderespons

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import java.util.*

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class Yrkeskoderespons {
    var betydninger: Map<String, List<Yrkeskode>>? = null
        set(betydninger) {
            field = LinkedHashMap(betydninger)
        }

}