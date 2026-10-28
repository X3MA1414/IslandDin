package com.chema.isla.data.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.chema.isla.domain.model.AppFilterMode
import com.chema.isla.domain.model.IslandSettings

/** Conversión entre [IslandSettings] y DataStore Preferences. */
object SettingsPreferencesMapper {

    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
        val SHOW_MEDIA = booleanPreferencesKey("show_media")
        val SHOW_CHARGING = booleanPreferencesKey("show_charging")
        val SHOW_IDLE = booleanPreferencesKey("show_idle_pill")
        val HIDE_LANDSCAPE = booleanPreferencesKey("hide_in_landscape")
        val HIDE_SYSTEM_HEADS_UP = booleanPreferencesKey("hide_system_heads_up")
        val NOTIFICATION_DURATION = longPreferencesKey("notification_duration")
        val CHARGING_DURATION = longPreferencesKey("charging_duration")
        val PILL_WIDTH = intPreferencesKey("pill_width")
        val PILL_HEIGHT = intPreferencesKey("pill_height")
        val EXPANDED_WIDTH = intPreferencesKey("expanded_width")
        val OFFSET_X = intPreferencesKey("offset_x")
        val OFFSET_Y = intPreferencesKey("offset_y")
        val ACCENT = longPreferencesKey("accent_color")
        val FILTER_MODE = stringPreferencesKey("app_filter_mode")
        val SELECTED = stringSetPreferencesKey("selected_packages")
    }

    fun fromPreferences(p: Preferences): IslandSettings {
        val d = IslandSettings()
        return IslandSettings(
            enabled = p[Keys.ENABLED] ?: d.enabled,
            showNotifications = p[Keys.SHOW_NOTIFICATIONS] ?: d.showNotifications,
            showMedia = p[Keys.SHOW_MEDIA] ?: d.showMedia,
            showCharging = p[Keys.SHOW_CHARGING] ?: d.showCharging,
            showIdlePill = p[Keys.SHOW_IDLE] ?: d.showIdlePill,
            hideInLandscape = p[Keys.HIDE_LANDSCAPE] ?: d.hideInLandscape,
            hideSystemHeadsUp = p[Keys.HIDE_SYSTEM_HEADS_UP] ?: d.hideSystemHeadsUp,
            notificationDurationMillis = p[Keys.NOTIFICATION_DURATION] ?: d.notificationDurationMillis,
            chargingDurationMillis = p[Keys.CHARGING_DURATION] ?: d.chargingDurationMillis,
            pillWidthDp = p[Keys.PILL_WIDTH] ?: d.pillWidthDp,
            pillHeightDp = p[Keys.PILL_HEIGHT] ?: d.pillHeightDp,
            expandedWidthDp = p[Keys.EXPANDED_WIDTH] ?: d.expandedWidthDp,
            offsetXDp = p[Keys.OFFSET_X] ?: d.offsetXDp,
            offsetYDp = p[Keys.OFFSET_Y] ?: d.offsetYDp,
            accentColorArgb = p[Keys.ACCENT] ?: d.accentColorArgb,
            appFilterMode = p[Keys.FILTER_MODE]
                ?.let { name -> AppFilterMode.entries.firstOrNull { it.name == name } }
                ?: d.appFilterMode,
            selectedPackages = p[Keys.SELECTED] ?: d.selectedPackages,
        ).normalized()
    }

    fun write(settings: IslandSettings, p: MutablePreferences) {
        p[Keys.ENABLED] = settings.enabled
        p[Keys.SHOW_NOTIFICATIONS] = settings.showNotifications
        p[Keys.SHOW_MEDIA] = settings.showMedia
        p[Keys.SHOW_CHARGING] = settings.showCharging
        p[Keys.SHOW_IDLE] = settings.showIdlePill
        p[Keys.HIDE_LANDSCAPE] = settings.hideInLandscape
        p[Keys.HIDE_SYSTEM_HEADS_UP] = settings.hideSystemHeadsUp
        p[Keys.NOTIFICATION_DURATION] = settings.notificationDurationMillis
        p[Keys.CHARGING_DURATION] = settings.chargingDurationMillis
        p[Keys.PILL_WIDTH] = settings.pillWidthDp
        p[Keys.PILL_HEIGHT] = settings.pillHeightDp
        p[Keys.EXPANDED_WIDTH] = settings.expandedWidthDp
        p[Keys.OFFSET_X] = settings.offsetXDp
        p[Keys.OFFSET_Y] = settings.offsetYDp
        p[Keys.ACCENT] = settings.accentColorArgb
        p[Keys.FILTER_MODE] = settings.appFilterMode.name
        p[Keys.SELECTED] = settings.selectedPackages
    }
}
