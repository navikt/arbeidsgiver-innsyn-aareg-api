package no.nav.tag.innsynAareg.client

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.retry.support.RetryTemplate
import java.io.IOException

class RetryInterceptor constructor(
    maxAttempts: Int,
    backoffPeriod: Long,
    vararg retryable: Class<out Throwable>
) : ClientHttpRequestInterceptor {

    private val retryTemplate = RetryTemplate.builder()
            .retryOn(listOf(*retryable))
            .traversingCauses()
            .maxAttempts(maxAttempts)
            .fixedBackoff(backoffPeriod)
            .build()

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        return retryTemplate.execute<ClientHttpResponse, IOException> {
            execution.execute(request, body)
        }
    }
}