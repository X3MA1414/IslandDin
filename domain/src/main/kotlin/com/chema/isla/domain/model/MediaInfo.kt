package com.chema.isla.domain.model

/** Estado de la reproducción multimedia activa. */
data class MediaInfo(
    val packageName: String,
    val title: String,
    val artist: String,
    val isPlaying: Boolean,
    val durationMillis: Long = 0L,
    val positionMillis: Long = 0L,
    val artworkKey: String? = null,
) {
    /** Progreso entre 0 y 1; 0 si la duración es desconocida. */
    val progress: Float
        get() = if (durationMillis <= 0L) 0f
        else (positionMillis.toFloat() / durationMillis).coerceIn(0f, 1f)
}

enum class MediaAction { PLAY_PAUSE, NEXT, PREVIOUS }
