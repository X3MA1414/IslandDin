package com.chema.isla.domain.policy

import com.chema.isla.domain.model.IslandContent
import com.chema.isla.domain.model.IslandSnapshot

/** Elige qué mostrar a partir del estado de todas las fuentes. */
fun interface ContentPriorityPolicy {
    fun select(snapshot: IslandSnapshot, nowMillis: Long): IslandContent
}

/**
 * Prioridad por defecto:
 * 1. Conexión/desconexión del cargador reciente.
 * 2. Notificación nueva (dentro de su tiempo de visualización).
 * 3. Música sonando.
 * 4. Reposo.
 */
class DefaultContentPriorityPolicy(
    private val notificationFilter: NotificationFilter,
) : ContentPriorityPolicy {

    override fun select(snapshot: IslandSnapshot, nowMillis: Long): IslandContent {
        val settings = snapshot.settings
        if (!settings.enabled) return IslandContent.Hidden

        val battery = snapshot.battery
        if (settings.showCharging && battery != null &&
            isFresh(battery.chargingChangedAtMillis, settings.chargingDurationMillis, nowMillis)
        ) {
            return IslandContent.Charging(battery)
        }

        if (settings.showNotifications) {
            snapshot.notifications
                .filter { notificationFilter.isAllowed(it, settings) }
                .filter { isFresh(it.postedAtMillis, settings.notificationDurationMillis, nowMillis) }
                .maxByOrNull { it.postedAtMillis }
                ?.let { return IslandContent.Notification(it) }
        }

        val media = snapshot.media
        if (settings.showMedia && media != null && media.isPlaying) {
            return IslandContent.Media(media)
        }

        return IslandContent.Idle
    }

    private fun isFresh(eventMillis: Long, durationMillis: Long, nowMillis: Long): Boolean {
        val age = nowMillis - eventMillis
        return age in 0 until durationMillis
    }
}
