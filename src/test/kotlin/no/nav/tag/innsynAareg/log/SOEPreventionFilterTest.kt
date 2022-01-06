package no.nav.tag.innsynAareg.log

import no.nav.tag.innsynAareg.log.SOEPreventionFilter.ExceptionLoopDetector.hasLoop
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

// https://jira.qos.ch/browse/LOGBACK-1454
class SOEPreventionFilterTest {
    @Test
    fun detectsLoopFromCircularInit() {
        val circular = Exception("foo")
        val e2 = Exception(circular)
        circular.initCause(e2)
        assertTrue(hasLoop(circular))
    }

    @Test
    fun detectsLoopFromCircularSupressed() {
        val circular = Exception("foo")
        val e2 = Exception(circular)
        circular.addSuppressed(e2)
        assertTrue(hasLoop(circular))
    }

    @Test
    fun noFalsePositive() {
        assertFalse(hasLoop(RuntimeException("foo")))
    }
}