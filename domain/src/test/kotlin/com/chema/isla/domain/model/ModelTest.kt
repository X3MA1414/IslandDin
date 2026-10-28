package com.chema.isla.domain.model

import com.chema.isla.domain.fakes.media
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelTest {

    @Test
    fun `normalized clamps every value into its range`() {
        val settings = IslandSettings(
            notificationDurationMillis = 100,
            chargingDurationMillis = 999_999,
            pillWidthDp = 5,
            pillHeightDp = 500,
            expandedWidthDp = 9_999,
            offsetXDp = -500,
            offsetYDp = 500,
        ).normalized()

        assertEquals(IslandSettings.DURATION_RANGE.first, settings.notificationDurationMillis)
        assertEquals(IslandSettings.DURATION_RANGE.last, settings.chargingDurationMillis)
        assertEquals(IslandSettings.PILL_WIDTH_RANGE.first, settings.pillWidthDp)
        assertEquals(IslandSettings.PILL_HEIGHT_RANGE.last, settings.pillHeightDp)
        assertEquals(IslandSettings.EXPANDED_WIDTH_RANGE.last, settings.expandedWidthDp)
        assertEquals(IslandSettings.OFFSET_RANGE.first, settings.offsetXDp)
        assertEquals(IslandSettings.OFFSET_RANGE.last, settings.offsetYDp)
    }

    @Test
    fun `default settings are already normalized`() {
        assertEquals(IslandSettings(), IslandSettings().normalized())
    }

    @Test
    fun `media progress is zero when duration is unknown`() {
        assertEquals(0f, media().copy(durationMillis = 0, positionMillis = 500).progress)
    }

    @Test
    fun `media progress is clamped between 0 and 1`() {
        assertEquals(0.5f, media().copy(durationMillis = 1000, positionMillis = 500).progress)
        assertEquals(1f, media().copy(durationMillis = 1000, positionMillis = 5000).progress)
    }

    @Test
    fun `notification without title and text has no visible content`() {
        val n = IslandNotification("k", "p", "App", " ", "", 0)
        assertFalse(n.hasVisibleContent)
        assertTrue(n.copy(text = "hola").hasVisibleContent)
    }
}
