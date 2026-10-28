package com.chema.isla.data.notification

import com.chema.isla.domain.model.IslandNotification
import com.chema.isla.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Puerta de entrada para que el NotificationListenerService publique cambios. */
interface NotificationSink {
    fun replaceAll(notifications: List<IslandNotification>)
    fun upsert(notification: IslandNotification)
    fun remove(key: String)
}

/** Repositorio en memoria alimentado por el listener de notificaciones. */
class InMemoryNotificationRepository : NotificationRepository, NotificationSink {

    private val state = MutableStateFlow<List<IslandNotification>>(emptyList())
    override val notifications: StateFlow<List<IslandNotification>> = state.asStateFlow()

    override fun replaceAll(notifications: List<IslandNotification>) {
        state.value = notifications.distinctBy { it.key }
    }

    /**
     * Inserta o actualiza. Si una app vuelve a publicar la misma notificación sin
     * cambiar su contenido, se conserva la hora original para no volver a mostrarla.
     */
    override fun upsert(notification: IslandNotification) {
        state.update { current ->
            val previous = current.firstOrNull { it.key == notification.key }
            val toStore = if (previous != null && previous.hasSameContentAs(notification)) {
                notification.copy(postedAtMillis = previous.postedAtMillis)
            } else {
                notification
            }
            current.filterNot { it.key == notification.key } + toStore
        }
    }

    private fun IslandNotification.hasSameContentAs(other: IslandNotification): Boolean =
        title == other.title && text == other.text

    override fun remove(key: String) {
        state.update { current -> current.filterNot { it.key == key } }
    }
}
