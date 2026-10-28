package com.chema.isla.domain.model

/** Lo que la isla debe mostrar en un instante dado. */
sealed interface IslandContent {
    /** La isla no se dibuja (desactivada por el usuario). */
    data object Hidden : IslandContent

    /** Píldora en reposo alrededor de la cámara. */
    data object Idle : IslandContent

    data class Notification(val notification: IslandNotification) : IslandContent

    data class Media(val media: MediaInfo) : IslandContent

    data class Charging(val battery: BatteryStatus) : IslandContent
}

/** Fotografía de todas las fuentes de datos, usada por las políticas de prioridad. */
data class IslandSnapshot(
    val notifications: List<IslandNotification>,
    val media: MediaInfo?,
    val battery: BatteryStatus?,
    val settings: IslandSettings,
)
