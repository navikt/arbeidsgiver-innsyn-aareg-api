package no.nav.tag.innsynAareg.utils

import no.nav.metrics.MetricsFactory
import no.nav.metrics.Timer

fun <T> withTimer(timerName: String, body: () -> T): T {
    val timer: Timer = MetricsFactory.createTimer(timerName).start()
    val result: T = body()
    timer.stop().report()
    return result
}