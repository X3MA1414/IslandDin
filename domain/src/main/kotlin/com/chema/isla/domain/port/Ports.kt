package com.chema.isla.domain.port

import kotlinx.coroutines.flow.Flow

/** Órdenes a la sesión multimedia activa. */
interface MediaCommands {
    fun play()
    fun pause()
    fun next()
    fun previous()
}

/** Acciones sobre una notificación concreta. */
interface NotificationActions {
    fun open(key: String): Boolean
    fun dismiss(key: String)
}

/** Reloj inyectable para poder testear lógica dependiente del tiempo. */
fun interface Clock {
    fun nowMillis(): Long
}

/** Emite periódicamente para reevaluar contenidos que caducan con el tiempo. */
interface Ticker {
    val ticks: Flow<Unit>
}
