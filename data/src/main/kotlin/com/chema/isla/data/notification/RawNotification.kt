package com.chema.isla.data.notification

/**
 * Datos crudos extraídos de una StatusBarNotification.
 * Es un DTO sin dependencias de Android para que el mapeo sea testeable en la JVM.
 */
data class RawNotification(
    val key: String,
    val packageName: String,
    val appLabel: String?,
    val title: CharSequence?,
    val text: CharSequence?,
    val bigText: CharSequence?,
    val postTime: Long,
    val isOngoing: Boolean,
    val isGroupSummary: Boolean,
    val template: String?,
)
