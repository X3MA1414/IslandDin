package com.chema.isla.data.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.chema.isla.domain.model.BatteryStatus
import com.chema.isla.domain.port.Clock
import com.chema.isla.domain.repository.BatteryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Escucha ACTION_BATTERY_CHANGED mientras haya algún servicio activo. */
class AndroidBatteryRepository(
    private val context: Context,
    private val clock: Clock,
) : BatteryRepository {

    private val state = MutableStateFlow<BatteryStatus?>(null)
    override val status: StateFlow<BatteryStatus?> = state.asStateFlow()
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = handle(intent)
    }

    fun start() {
        if (registered) return
        registered = true
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.let(::handle)
    }

    fun stop() {
        if (!registered) return
        registered = false
        runCatching { context.unregisterReceiver(receiver) }
    }

    private fun handle(intent: Intent) {
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        state.value = BatteryStateReducer.reduce(
            previous = state.value,
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0),
            scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100),
            isCharging = charging,
            nowMillis = clock.nowMillis(),
        )
    }
}
