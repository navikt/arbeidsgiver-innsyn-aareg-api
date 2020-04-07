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
    private var betydninger: Map<String?, List<Yrkeskode?>?>? = null
    fun getBetydninger(): Map<String?, List<Yrkeskode?>?>? {
        return betydninger
    }

    fun setBetydninger(betydninger: Map<String?, List<Yrkeskode?>?>?) {
        this.betydninger = LinkedHashMap(betydninger)
    }
}