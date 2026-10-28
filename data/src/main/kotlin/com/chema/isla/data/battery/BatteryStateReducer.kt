package com.chema.isla.data.battery

import com.chema.isla.domain.model.BatteryStatus

/**
 * Calcula el nuevo estado de batería a partir del anterior.
 * Solo actualiza la marca temporal cuando cambia el estado de carga, de modo que
 * la animación de "cargando" aparezca únicamente al conectar o desconectar.
 */
object BatteryStateReducer {

    fun reduce(previous: BatteryStatus?, level: Int, scale: Int, isCharging: Boolean, nowMillis: Long): BatteryStatus {
        val percent = if (scale > 0) (level * 100 / scale).coerceIn(0, 100) else level.coerceIn(0, 100)
        val changedAt = when {
            previous == null -> Long.MIN_VALUE / 2 // Primera lectura: no animar al arrancar.
            previous.isCharging != isCharging -> nowMillis
            else -> previous.chargingChangedAtMillis
        }
        return BatteryStatus(levelPercent = percent, isCharging = isCharging, chargingChangedAtMillis = changedAt)
    }
}
