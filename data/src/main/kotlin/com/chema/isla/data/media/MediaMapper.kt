package com.chema.isla.data.media

import com.chema.isla.domain.model.MediaInfo

/** DTO sin dependencias de Android con los datos de una sesión multimedia. */
data class RawMedia(
    val packageName: String,
    val title: CharSequence?,
    val artist: CharSequence?,
    val album: CharSequence?,
    val isPlaying: Boolean,
    val durationMillis: Long,
    val positionMillis: Long,
    val hasArtwork: Boolean,
)

object MediaMapper {

    /** Devuelve null si la sesión no tiene información que mostrar. */
    fun map(raw: RawMedia): MediaInfo? {
        val title = raw.title?.toString()?.trim().orEmpty()
        if (title.isEmpty()) return null
        val artist = (raw.artist ?: raw.album)?.toString()?.trim().orEmpty()
        return MediaInfo(
            packageName = raw.packageName,
            title = title,
            artist = artist,
            isPlaying = raw.isPlaying,
            durationMillis = raw.durationMillis.coerceAtLeast(0L),
            positionMillis = raw.positionMillis.coerceAtLeast(0L),
            artworkKey = if (raw.hasArtwork) artworkKey(raw.packageName, title, artist) else null,
        )
    }

    fun artworkKey(packageName: String, title: String, artist: String): String =
        "$packageName|$title|$artist"
}
