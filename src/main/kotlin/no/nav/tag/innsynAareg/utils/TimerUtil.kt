package no.nav.tag.innsynAareg.utils

import no.nav.metrics.MetricsFactory
import no.nav.metrics.Timer

fun <T> withTimer(timerName: String, body: () -> T): T {
    val timer = MetricsFactory.createTimer(timerName).start()
    try {
        return body().also {
            timer.stop().report()
        }
    } catch (e: Exception) {
        timer.stop()
        throw e
    }
}