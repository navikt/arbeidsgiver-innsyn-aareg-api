package no.nav.tag.innsynAareg.service.sts

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@SpringBootTest
@RunWith(SpringRunner::class)
@ActiveProfiles("local")
@TestPropertySource(properties = ["mock.port=8082"])
internal class STSClientTest() {
    @Autowired
    lateinit var stsClient: STSClient

    @Test
    fun getToken() {
        val result = stsClient.token?.access_token;
        Assert.assertEquals(result, "eyJraWQiOiJiZGYyMzY1My1kMGI2LTQ3YWUtOWYyMi0zY2RjODkyMjllYTIiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZkaXR0LW5hdi1hcmJlaWQiLCJhdWQiOlsic3J2ZGl0dC1uYXYtYXJiZWlkIiwicHJlcHJvZC5sb2NhbCJdLCJ2ZXIiOiIxLjAiLCJuYmYiOjE1NTQxODgzODcsImF6cCI6InNydmRpdHQtbmF2LWFyYmVpZCIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1NTQxODgzODcsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTU1NDE5MTk4NywiaWF0IjoxNTU0MTg4Mzg3LCJqdGkiOiJjMGU4MTA5Ni1kZTNkLTQzYTgtYWYxZS1hNzA1MWRiYTBiY2QifQ.k_sEiRcZShidP7C79r6nKwiIl2mgfA9CJUagqwszGiPOVwKtOHhba0ruXGmQhLHOem9I9EjJRgBYpS0I4tjyxuCI69MDbc33_tbIliCz6xIkd8J8O72scPtxFWlKGX_zqUORVgpmPZgoQXBXJFSSpNNT4bfno6I_oLj25bLEAnSiD_6d6b1oKSj-6EVok88jpVkbnAk1WkGRznug-dCUGtyEk4NOSZroZRii3J8uDdYH0izawjAyNrixUQeQjgtLKEOODyoPDvuSerwP01dewHhJuU80BWI20QJlkGbcAIUknTKo4NS0YBasjg-XavR7NfA95a2u2ru3RZB0CHSKIg");
    }
}