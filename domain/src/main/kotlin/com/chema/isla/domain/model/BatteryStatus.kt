package com.chema.isla.domain.model

/**
 * Estado de la batería.
 * [chargingChangedAtMillis] indica cuándo se conectó o desconectó el cargador por última vez.
 */
data class BatteryStatus(
    val levelPercent: Int,
    val isCharging: Boolean,
    val chargingChangedAtMillis: Long,
)
