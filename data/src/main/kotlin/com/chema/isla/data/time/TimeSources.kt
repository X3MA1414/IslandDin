package com.chema.isla.data.time

import com.chema.isla.domain.port.Clock
import com.chema.isla.domain.port.Ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object SystemClock : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}

/** Emite cada [periodMillis] mientras haya alguien escuchando. */
class IntervalTicker(private val periodMillis: Long = 500L) : Ticker {
    override val ticks: Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(periodMillis)
        }
    }
}
