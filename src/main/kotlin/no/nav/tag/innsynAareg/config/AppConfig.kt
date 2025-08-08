package no.nav.tag.innsynAareg.config

import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate


@Configuration
class AppConfig {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate? {
        return builder.build()
    }

    /**
     * As of Spring Boot 3.4 https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes#upgrading-from-spring-boot-33
     * The default `RestTemplate` no longer uses the Simple JDK HttpURLConnection (SimpleClientHttpRequestFactory), but selects one from the classpath. In this app apache due to transitive dependency.
     * This results in intermittent connection issues often resulting in errors like "org.springframework.web.client.ResourceAccessException: I/O error on POST request for https://login.microsoftonline.com..."
     *
     * This is now changed to apache5 explicitly. And with this bean we force the use of proxy config from system properties, which is needed for the app to work in the NAV environment.
     */
    @Bean
    fun clientHttpRequestFactory() = HttpComponentsClientHttpRequestFactory(
        HttpClients.custom()
                .useSystemProperties()
                .setRetryStrategy(DefaultHttpRequestRetryStrategy())
                .build()
    )
}