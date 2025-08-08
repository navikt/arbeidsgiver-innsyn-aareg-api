package no.nav.tag.innsynAareg.config

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import java.net.ProxySelector
import java.net.http.HttpClient
import java.util.function.Consumer


@Configuration
class AppConfig {
    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate? {
        return builder.build()
    }


    /**
     * As of Spring Boot 3.4 https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes#upgrading-from-spring-boot-33
     * The default `RestTemplate` uses the JDK HTTP client, which does not support proxy configuration via system properties in the same way the
     * Simple JDK HttpURLConnection (SimpleClientHttpRequestFactory) did.
     * This results in intermittent connection issues often resulting in errors like "org.springframework.web.client.ResourceAccessException: I/O error on POST request for https://login.microsoftonline.com..."
     *
     * To resolve this, we configure the `RestTemplate` to use a `ClientHttpRequestFactoryBuilder` that explicitly sets the proxy selector.
     */
    @Bean
    fun clientHttpRequestFactoryBuilder(): ClientHttpRequestFactoryBuilder<*> {
        return ClientHttpRequestFactoryBuilder.jdk()
            .withHttpClientCustomizer { builder: HttpClient.Builder ->
                builder.proxy(ProxySelector.getDefault())
            }
    }
}