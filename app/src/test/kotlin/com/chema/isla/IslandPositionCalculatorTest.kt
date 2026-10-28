package com.chema.isla

import com.chema.isla.overlay.CutoutBounds
import com.chema.isla.overlay.IslandPosition
import com.chema.isla.overlay.IslandPositionCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class IslandPositionCalculatorTest {

    @Test
    fun `centers the pill vertically on a centered camera`() {
        // Pantalla de 1440 px, cámara de 60 px centrada a 30..90 px.
        val position = IslandPositionCalculator.compute(
            cutout = CutoutBounds(690, 30, 750, 90),
            screenWidthPx = 1440,
            density = 3f,
            pillHeightDp = 32,
            offsetXDp = 0,
            offsetYDp = 0,
            fallbackTopPx = 24,
        )
        assertEquals(IslandPosition(xPx = 0, yPx = 60 - 48), position)
    }

    @Test
    fun `off-center camera shifts the window horizontally and offsets apply`() {
        val position = IslandPositionCalculator.compute(
            cutout = CutoutBounds(100, 30, 160, 90),
            screenWidthPx = 1440,
            density = 2f,
            pillHeightDp = 30,
            offsetXDp = 5,
            offsetYDp = 2,
            fallbackTopPx = 0,
        )
        assertEquals(IslandPosition(xPx = 130 - 720 + 10, yPx = 60 - 30 + 4), position)
    }

    @Test
    fun `without cutout uses the fallback and never goes above the screen`() {
        val position = IslandPositionCalculator.compute(
            cutout = null,
            screenWidthPx = 1080,
            density = 2f,
            pillHeightDp = 30,
            offsetXDp = 0,
            offsetYDp = -60,
            fallbackTopPx = 16,
        )
        assertEquals(IslandPosition(0, 0), position)
    }
}
