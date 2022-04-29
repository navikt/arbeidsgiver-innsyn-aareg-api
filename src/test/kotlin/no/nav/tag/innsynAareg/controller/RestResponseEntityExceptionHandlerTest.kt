package no.nav.tag.innsynAareg.controller

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.context.request.WebRequest

@SpringBootTest(webEnvironment = RANDOM_PORT)
@EnableMockOAuth2Server
@ExtendWith(SpringExtension::class)
class RestResponseEntityExceptionHandlerTest {

    @Autowired
    lateinit var restResponseEntityExceptionHandler: RestResponseEntityExceptionHandler

    @Test
    fun loggerMedSirkulærReferanseFraInit() {
        val circular = Exception("foo")
        val e2 = Exception(circular)
        circular.initCause(e2)
        Assertions.assertDoesNotThrow {
            restResponseEntityExceptionHandler.handleInternalError(
                RuntimeException("wat", circular),
                mock(WebRequest::class.java)
            )
        }
    }

    @Test
    fun loggerMedSirkulærReferanseFraSupressed() {
        val circular = Exception("foo")
        val e2 = Exception(circular)
        circular.addSuppressed(e2)
        Assertions.assertDoesNotThrow {
            restResponseEntityExceptionHandler.handleInternalError(
                RuntimeException("wat", circular),
                mock(WebRequest::class.java)
            )
        }
    }
}