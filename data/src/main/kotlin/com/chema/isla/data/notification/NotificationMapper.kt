package com.chema.isla.data.notification

import com.chema.isla.domain.model.IslandNotification

/** Traduce [RawNotification] al modelo de dominio. */
object NotificationMapper {

    private val MEDIA_TEMPLATES = setOf(
        "android.app.Notification\$MediaStyle",
        "android.app.Notification\$DecoratedMediaCustomViewStyle",
    )

    fun map(raw: RawNotification): IslandNotification = IslandNotification(
        key = raw.key,
        packageName = raw.packageName,
        appLabel = raw.appLabel?.takeIf { it.isNotBlank() } ?: raw.packageName,
        title = raw.title?.toString()?.trim().orEmpty(),
        text = (raw.bigText ?: raw.text)?.toString()?.trim().orEmpty(),
        postedAtMillis = raw.postTime,
        isOngoing = raw.isOngoing,
        isGroupSummary = raw.isGroupSummary,
        isMediaStyle = raw.template in MEDIA_TEMPLATES,
    )
}
