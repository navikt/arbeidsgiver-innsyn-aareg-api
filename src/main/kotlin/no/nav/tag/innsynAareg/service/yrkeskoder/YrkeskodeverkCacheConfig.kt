package no.nav.tag.innsynAareg.service.yrkeskoder

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class YrkeskodeverkCacheConfig {
    @Bean
    fun kodeverkCache(): CaffeineCache {
        return CaffeineCache(
            YRKESKODE_CACHE,
            Caffeine.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(59, TimeUnit.MINUTES)
                .recordStats()
                .build()
        )
    }

    companion object {
        const val YRKESKODE_CACHE = "yrkeskode_cache"
    }
}