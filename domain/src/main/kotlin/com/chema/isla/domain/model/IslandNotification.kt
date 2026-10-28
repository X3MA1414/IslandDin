package com.chema.isla.domain.model

/**
 * Notificación de otra app, ya traducida a un modelo independiente de Android.
 */
data class IslandNotification(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postedAtMillis: Long,
    val isOngoing: Boolean = false,
    val isGroupSummary: Boolean = false,
    val isMediaStyle: Boolean = false,
) {
    val hasVisibleContent: Boolean get() = title.isNotBlank() || text.isNotBlank()
}
