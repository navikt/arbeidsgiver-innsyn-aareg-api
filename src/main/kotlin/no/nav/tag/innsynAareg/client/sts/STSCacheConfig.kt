package no.nav.tag.innsynAareg.client.sts

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class STSCacheConfig {
    companion object {
        const val STS_CACHE = "sts_cache"
    }

    @Bean
    fun stsCache(): CaffeineCache {
        return CaffeineCache(
            STS_CACHE,
            Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(40, TimeUnit.MINUTES)
                .recordStats()
                .build()
        )
    }
}
