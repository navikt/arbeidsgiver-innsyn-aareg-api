package no.nav.tag.innsynAareg.log

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import org.slf4j.Marker

/**
 * Dette filteret er en hack for å unngå java.lang.StackOverflowError siden spring boot ikke støtter logback 1.3 P.T.
 *
 * ref:
 * https://jira.qos.ch/browse/LOGBACK-1454
 * https://github.com/spring-projects/spring-boot/issues/12649
 *
 * obs: Hele denne klassen kan fjernes når vi er over på en versjon av logback som takler exceptions med sirkusreferanser
 */
class SOEPreventionFilter : TurboFilter() {
        override fun decide(
            marker: Marker?,
            logger: Logger,
            level: Level?,
            format: String?,
            params: Array<Any>?,
            t: Throwable?,
        ): FilterReply {
        if (t != null) { // log.level("", ex)
            if (ExceptionLoopDetector.hasLoop(t)) {
                logger.log(marker,
                    logger.name,
                    Level.toLocationAwareLoggerInteger(level),
                    format,
                    params,
                    RuntimeException(t.message))
                return FilterReply.DENY
            }
        } else { // log.level("lorum {} ipsum {}", str1, obj2, ex)
            val lastArg = params?.last()
            if (lastArg is Throwable) {
                if (ExceptionLoopDetector.hasLoop(lastArg)) {
                    val copy = arrayOf(*params)
                    copy[params.size - 1] = RuntimeException(lastArg.message)
                    logger.log(marker, logger.name, Level.toLocationAwareLoggerInteger(level), format, copy, null)
                    return FilterReply.DENY
                }
                return FilterReply.NEUTRAL
            }
        }
        return FilterReply.NEUTRAL
    }

    internal object ExceptionLoopDetector {
        /**
         * test om throwable lar seg proxye i logback
         */
        fun hasLoop(t: Throwable): Boolean {
            return try {
                ThrowableProxy(t)
                false
            } catch (e: StackOverflowError) {
                true
            }
        }
    }
}