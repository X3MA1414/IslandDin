package com.chema.isla.data.notification

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log
import com.chema.isla.domain.port.NotificationActions
import java.util.concurrent.ConcurrentHashMap

/**
 * Guarda el PendingIntent de cada notificación para poder abrirla desde la isla,
 * y delega el descarte en el listener activo.
 */
class AndroidNotificationActions(
    private val context: Context,
) : NotificationActions {

    private val intents = ConcurrentHashMap<String, PendingIntent>()

    @Volatile
    private var dismisser: ((String) -> Unit)? = null

    fun register(key: String, intent: PendingIntent?) {
        if (intent == null) intents.remove(key) else intents[key] = intent
    }

    fun unregister(key: String) {
        intents.remove(key)
    }

    fun attachDismisser(dismisser: ((String) -> Unit)?) {
        this.dismisser = dismisser
    }

    override fun open(key: String): Boolean {
        val intent = intents[key] ?: return false
        return try {
            intent.send(context, 0, null, null, null, null, launchOptions())
            true
        } catch (e: PendingIntent.CanceledException) {
            Log.w(TAG, "PendingIntent cancelado para $key", e)
            intents.remove(key)
            false
        }
    }

    /** Android 14+: permite abrir la app desde la isla aunque estemos en segundo plano. */
    private fun launchOptions() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityOptions.makeBasic()
                .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                .toBundle()
        } else {
            null
        }

    override fun dismiss(key: String) {
        dismisser?.invoke(key)
    }

    private companion object {
        const val TAG = "NotificationActions"
    }
}
