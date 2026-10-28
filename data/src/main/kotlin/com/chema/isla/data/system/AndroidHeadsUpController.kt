package com.chema.isla.data.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import com.chema.isla.domain.port.SystemHeadsUpController

/**
 * Cambia el ajuste global "heads_up_notifications_enabled".
 * Necesita WRITE_SECURE_SETTINGS, que solo se puede conceder por ADB:
 *   adb shell pm grant com.chema.isla android.permission.WRITE_SECURE_SETTINGS
 */
class AndroidHeadsUpController(private val context: Context) : SystemHeadsUpController {

    override val canControl: Boolean
        get() = context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED

    override fun setSystemHeadsUpEnabled(enabled: Boolean) {
        val value = if (enabled) 1 else 0
        runCatching {
            val resolver = context.contentResolver
            if (Settings.Global.getInt(resolver, KEY, 1) != value) {
                Settings.Global.putInt(resolver, KEY, value)
            }
        }.onFailure { Log.w(TAG, "No se pudo cambiar $KEY", it) }
    }

    companion object {
        const val KEY = "heads_up_notifications_enabled"
        private const val TAG = "HeadsUpController"
        const val GRANT_COMMAND =
            "adb shell pm grant com.chema.isla android.permission.WRITE_SECURE_SETTINGS"
    }
}
