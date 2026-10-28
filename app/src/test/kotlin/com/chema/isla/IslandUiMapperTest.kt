package com.chema.isla

import com.chema.isla.domain.model.BatteryStatus
import com.chema.isla.domain.model.IslandContent
import com.chema.isla.domain.model.IslandNotification
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.MediaInfo
import com.chema.isla.island.IslandBody
import com.chema.isla.island.IslandUiMapper
import com.chema.isla.island.IslandUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IslandUiMapperTest {

    private val mapper = IslandUiMapper()
    private val settings = IslandSettings(pillWidthDp = 100, pillHeightDp = 30, expandedWidthDp = 350)
    private val media = MediaInfo("pkg", "t", "a", isPlaying = true)

    @Test
    fun `idle shows a pill with the configured size`() {
        val state = mapper.map(IslandContent.Idle, settings, mediaExpanded = false, isLandscape = false)
        assertEquals(IslandUiState(true, 100, 30, 15, settings.accentColorArgb, IslandBody.Empty), state)
    }

    @Test
    fun `idle is hidden when the idle pill is disabled`() {
        val state = mapper.map(IslandContent.Idle, settings.copy(showIdlePill = false), false, false)
        assertEquals(IslandUiState.Hidden, state)
    }

    @Test
    fun `landscape hides the island only when configured`() {
        assertEquals(IslandUiState.Hidden, mapper.map(IslandContent.Idle, settings, false, isLandscape = true))
        assertTrue(mapper.map(IslandContent.Idle, settings.copy(hideInLandscape = false), false, true).visible)
    }

    @Test
    fun `charging uses the compact width`() {
        val state = mapper.map(IslandContent.Charging(BatteryStatus(80, true, 0)), settings, false, false)
        assertEquals(100 + 2 * IslandUiMapper.COMPACT_SIDE_DP, state.widthDp)
        assertEquals(IslandBody.Charging(80, true), state.body)
    }

    @Test
    fun `notification uses the expanded width`() {
        val n = IslandNotification("k", "pkg", "App", "Hola", "texto", 0)
        val state = mapper.map(IslandContent.Notification(n), settings, false, false)
        assertEquals(350, state.widthDp)
        assertEquals(IslandUiMapper.NOTIFICATION_HEIGHT_DP, state.heightDp)
        assertEquals(IslandBody.Notification("k", "pkg", "App", "Hola", "texto"), state.body)
    }

    @Test
    fun `media is compact until expanded`() {
        val compact = mapper.map(IslandContent.Media(media), settings, mediaExpanded = false, isLandscape = false)
        val expanded = mapper.map(IslandContent.Media(media), settings, mediaExpanded = true, isLandscape = false)
        assertEquals(IslandBody.MediaCompact(media), compact.body)
        assertEquals(IslandBody.MediaExpanded(media), expanded.body)
        assertEquals(IslandUiMapper.MEDIA_EXPANDED_HEIGHT_DP, expanded.heightDp)
    }

    @Test
    fun `hidden content maps to hidden state`() {
        assertEquals(IslandUiState.Hidden, mapper.map(IslandContent.Hidden, settings, false, false))
    }
}
