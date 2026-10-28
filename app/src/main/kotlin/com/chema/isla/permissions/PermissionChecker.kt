package com.chema.isla.permissions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.chema.isla.domain.port.SystemHeadsUpController
import com.chema.isla.service.IslandAccessibilityService

data class PermissionStatus(
    val accessibility: Boolean,
    val notificationListener: Boolean,
    val ignoringBatteryOptimizations: Boolean,
    /** Permiso opcional (ADB) para ocultar los avisos emergentes del sistema. */
    val canHideSystemPopups: Boolean = false,
) {
    val allRequiredGranted: Boolean get() = accessibility && notificationListener
}

interface PermissionChecker {
    fun status(): PermissionStatus
    fun accessibilitySettingsIntent(): Intent
    fun notificationListenerSettingsIntent(): Intent
    fun batteryOptimizationIntent(): Intent
    fun appDetailsIntent(): Intent
}

class AndroidPermissionChecker(
    private val context: Context,
    private val headsUpController: SystemHeadsUpController,
) : PermissionChecker {

    override fun status() = PermissionStatus(
        accessibility = isAccessibilityEnabled(),
        notificationListener = context.packageName in
            NotificationManagerCompat.getEnabledListenerPackages(context),
        ignoringBatteryOptimizations = context.getSystemService(PowerManager::class.java)
            .isIgnoringBatteryOptimizations(context.packageName),
        canHideSystemPopups = headsUpController.canControl,
    )

    private fun isAccessibilityEnabled(): Boolean {
        val expected = ComponentName(context, IslandAccessibilityService::class.java)
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
    }

    override fun accessibilitySettingsIntent() =
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).newTask()

    override fun notificationListenerSettingsIntent() =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).newTask()

    @android.annotation.SuppressLint("BatteryLife")
    override fun batteryOptimizationIntent() =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
            .newTask()

    override fun appDetailsIntent() =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            .newTask()

    private fun Intent.newTask() = addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
