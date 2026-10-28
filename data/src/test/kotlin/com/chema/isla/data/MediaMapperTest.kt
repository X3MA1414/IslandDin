package com.chema.isla.data

import com.chema.isla.data.media.MediaMapper
import com.chema.isla.data.media.RawMedia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaMapperTest {

    private val raw = RawMedia(
        packageName = "com.spotify.music",
        title = "Canción",
        artist = "Artista",
        album = "Álbum",
        isPlaying = true,
        durationMillis = 1000,
        positionMillis = 250,
        hasArtwork = true,
    )

    @Test
    fun `maps a playing session`() {
        val info = MediaMapper.map(raw)!!
        assertEquals("Canción", info.title)
        assertEquals("Artista", info.artist)
        assertEquals(0.25f, info.progress)
        assertEquals(MediaMapper.artworkKey("com.spotify.music", "Canción", "Artista"), info.artworkKey)
    }

    @Test
    fun `session without title is ignored`() {
        assertNull(MediaMapper.map(raw.copy(title = "  ")))
    }

    @Test
    fun `album is used when artist is missing`() {
        assertEquals("Álbum", MediaMapper.map(raw.copy(artist = null))!!.artist)
    }

    @Test
    fun `negative times are clamped and missing artwork has no key`() {
        val info = MediaMapper.map(raw.copy(durationMillis = -1, positionMillis = -5, hasArtwork = false))!!
        assertEquals(0L, info.durationMillis)
        assertEquals(0L, info.positionMillis)
        assertNull(info.artworkKey)
    }
}
