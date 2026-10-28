package com.chema.isla.island

import com.chema.isla.domain.model.IslandContent
import com.chema.isla.domain.model.IslandSettings

/** Convierte contenido de dominio + ajustes en un estado listo para dibujar. */
class IslandUiMapper {

    fun map(
        content: IslandContent,
        settings: IslandSettings,
        mediaExpanded: Boolean,
        isLandscape: Boolean,
    ): IslandUiState {
        if (content is IslandContent.Hidden) return IslandUiState.Hidden
        if (isLandscape && settings.hideInLandscape) return IslandUiState.Hidden

        val pillH = settings.pillHeightDp
        val compactW = settings.pillWidthDp + 2 * COMPACT_SIDE_DP
        val accent = settings.accentColorArgb

        fun pill(width: Int, height: Int, body: IslandBody, corner: Int = height / 2) =
            IslandUiState(true, width, height, corner, accent, body)

        return when (content) {
            IslandContent.Hidden -> IslandUiState.Hidden
            IslandContent.Idle ->
                if (settings.showIdlePill) pill(settings.pillWidthDp, pillH, IslandBody.Empty)
                else IslandUiState.Hidden
            is IslandContent.Charging ->
                pill(compactW, pillH, IslandBody.Charging(content.battery.levelPercent, content.battery.isCharging))
            is IslandContent.Notification -> {
                val n = content.notification
                pill(
                    width = settings.expandedWidthDp,
                    height = NOTIFICATION_HEIGHT_DP,
                    body = IslandBody.Notification(n.key, n.packageName, n.appLabel, n.title, n.text),
                    corner = EXPANDED_CORNER_DP,
                )
            }
            is IslandContent.Media ->
                if (mediaExpanded) {
                    pill(
                        width = settings.expandedWidthDp,
                        height = MEDIA_EXPANDED_HEIGHT_DP,
                        body = IslandBody.MediaExpanded(content.media),
                        corner = EXPANDED_CORNER_DP,
                    )
                } else {
                    pill(compactW, pillH, IslandBody.MediaCompact(content.media))
                }
        }
    }

    companion object {
        const val COMPACT_SIDE_DP = 56
        const val NOTIFICATION_HEIGHT_DP = 76
        const val MEDIA_EXPANDED_HEIGHT_DP = 168
        const val EXPANDED_CORNER_DP = 32
    }
}
