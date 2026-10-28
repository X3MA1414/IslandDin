package com.chema.isla.island

import com.chema.isla.domain.model.MediaInfo

/** Contenido visual de la isla. */
sealed interface IslandBody {
    data object Empty : IslandBody
    data class Charging(val levelPercent: Int, val isCharging: Boolean) : IslandBody
    data class Notification(
        val key: String,
        val packageName: String,
        val appLabel: String,
        val title: String,
        val text: String,
    ) : IslandBody
    data class MediaCompact(val media: MediaInfo) : IslandBody
    data class MediaExpanded(val media: MediaInfo) : IslandBody
}

/** Estado completo que la vista de la isla dibuja, ya con medidas calculadas. */
data class IslandUiState(
    val visible: Boolean,
    val widthDp: Int,
    val heightDp: Int,
    val cornerDp: Int,
    val accentArgb: Long,
    val body: IslandBody,
) {
    companion object {
        val Hidden = IslandUiState(
            visible = false,
            widthDp = 0,
            heightDp = 0,
            cornerDp = 0,
            accentArgb = 0,
            body = IslandBody.Empty,
        )
    }
}

/** Qué debe hacer la capa Android cuando el usuario toca la isla. */
sealed interface IslandTapResult {
    data object None : IslandTapResult
    data class OpenNotification(val key: String) : IslandTapResult
    data class OpenApp(val packageName: String) : IslandTapResult
}
