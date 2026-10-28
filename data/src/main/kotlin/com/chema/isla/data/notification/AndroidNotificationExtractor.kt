package com.chema.isla.data.notification

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification

/** Extrae un [RawNotification] de una notificación del sistema. */
class AndroidNotificationExtractor(
    private val packageManager: PackageManager,
) {
    private val labelCache = HashMap<String, String>()

    fun extract(sbn: StatusBarNotification): RawNotification {
        val extras = sbn.notification.extras
        return RawNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            appLabel = labelFor(sbn.packageName),
            title = extras.getCharSequence(Notification.EXTRA_TITLE),
            text = extras.getCharSequence(Notification.EXTRA_TEXT),
            bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT),
            postTime = sbn.postTime,
            isOngoing = sbn.isOngoing,
            isGroupSummary = sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0,
            template = extras.getString(Notification.EXTRA_TEMPLATE),
        )
    }

    private fun labelFor(packageName: String): String? = labelCache.getOrPut(packageName) {
        runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }
}
