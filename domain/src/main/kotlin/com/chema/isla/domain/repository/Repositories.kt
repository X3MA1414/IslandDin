package com.chema.isla.domain.repository

import com.chema.isla.domain.model.BatteryStatus
import com.chema.isla.domain.model.IslandNotification
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.MediaInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/** Notificaciones activas en el sistema. */
interface NotificationRepository {
    val notifications: StateFlow<List<IslandNotification>>
}

/** Sesión multimedia activa, o null si no hay ninguna. */
interface MediaRepository {
    val media: StateFlow<MediaInfo?>
}

/** Estado de la batería, o null hasta recibir la primera lectura. */
interface BatteryRepository {
    val status: StateFlow<BatteryStatus?>
}

interface SettingsRepository {
    val settings: Flow<IslandSettings>
    suspend fun update(transform: (IslandSettings) -> IslandSettings)
}

/** Apps con icono en el lanzador. */
interface InstalledAppsRepository {
    suspend fun launchableApps(): List<com.chema.isla.domain.model.InstalledApp>
}
