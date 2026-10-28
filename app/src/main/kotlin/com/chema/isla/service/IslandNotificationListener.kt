package com.chema.isla.service

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.chema.isla.appContainer
import com.chema.isla.data.notification.NotificationMapper

/**
 * Recibe las notificaciones del sistema y las publica en el repositorio.
 * Este permiso también da acceso a las sesiones multimedia de otras apps.
 */
class IslandNotificationListener : NotificationListenerService() {

    private val container get() = appContainer

    override fun onListenerConnected() {
        super.onListenerConnected()
        container.notificationActions.attachDismisser { key -> runCatching { cancelNotification(key) } }
        val current = runCatching { activeNotifications?.toList().orEmpty() }.getOrDefault(emptyList())
        current.forEach(::registerIntent)
        container.notificationSink.replaceAll(current.map(::toDomain))
        container.mediaRepository.start(ComponentName(this, IslandNotificationListener::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        registerIntent(sbn)
        container.notificationSink.upsert(toDomain(sbn))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        container.notificationActions.unregister(sbn.key)
        container.notificationSink.remove(sbn.key)
    }

    override fun onListenerDisconnected() {
        container.notificationActions.attachDismisser(null)
        container.mediaRepository.stop()
        container.notificationSink.replaceAll(emptyList())
        super.onListenerDisconnected()
    }

    private fun registerIntent(sbn: StatusBarNotification) =
        container.notificationActions.register(sbn.key, sbn.notification.contentIntent)

    private fun toDomain(sbn: StatusBarNotification) =
        NotificationMapper.map(container.notificationExtractor.extract(sbn))
}
