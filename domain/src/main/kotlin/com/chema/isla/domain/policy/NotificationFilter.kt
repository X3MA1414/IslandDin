package com.chema.isla.domain.policy

import com.chema.isla.domain.model.AppFilterMode
import com.chema.isla.domain.model.IslandNotification
import com.chema.isla.domain.model.IslandSettings

/** Decide si una notificación es apta para mostrarse en la isla. */
fun interface NotificationFilter {
    fun isAllowed(notification: IslandNotification, settings: IslandSettings): Boolean
}

/**
 * Filtro por defecto: descarta notificaciones persistentes, resúmenes de grupo,
 * notificaciones multimedia (las gestiona la vista de música), las de la propia app
 * y las que no pasan el filtro de apps del usuario.
 */
class DefaultNotificationFilter(
    private val ownPackageName: String,
) : NotificationFilter {

    override fun isAllowed(notification: IslandNotification, settings: IslandSettings): Boolean =
        !notification.isOngoing &&
            !notification.isGroupSummary &&
            !notification.isMediaStyle &&
            notification.hasVisibleContent &&
            notification.packageName != ownPackageName &&
            passesAppFilter(notification.packageName, settings)

    private fun passesAppFilter(packageName: String, settings: IslandSettings): Boolean {
        val selected = packageName in settings.selectedPackages
        return when (settings.appFilterMode) {
            AppFilterMode.ALL_EXCEPT_SELECTED -> !selected
            AppFilterMode.ONLY_SELECTED -> selected
        }
    }
}
