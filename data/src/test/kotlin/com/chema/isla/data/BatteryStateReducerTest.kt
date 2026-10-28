package com.chema.isla.data

import com.chema.isla.data.battery.BatteryStateReducer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryStateReducerTest {

    @Test
    fun `first reading is never considered a recent change`() {
        val s = BatteryStateReducer.reduce(null, level = 50, scale = 100, isCharging = true, nowMillis = 1_000)
        assertEquals(50, s.levelPercent)
        assertTrue(1_000 - s.chargingChangedAtMillis > 1_000_000)
    }

    @Test
    fun `plugging in records the change time`() {
        val first = BatteryStateReducer.reduce(null, 50, 100, isCharging = false, nowMillis = 0)
        val plugged = BatteryStateReducer.reduce(first, 51, 100, isCharging = true, nowMillis = 5_000)
        assertEquals(5_000, plugged.chargingChangedAtMillis)
    }

    @Test
    fun `level updates keep the previous change time`() {
        val first = BatteryStateReducer.reduce(null, 50, 100, false, 0)
        val plugged = BatteryStateReducer.reduce(first, 51, 100, true, 5_000)
        val later = BatteryStateReducer.reduce(plugged, 60, 100, true, 90_000)
        assertEquals(5_000, later.chargingChangedAtMillis)
        assertEquals(60, later.levelPercent)
    }

    @Test
    fun `level is scaled and clamped`() {
        assertEquals(50, BatteryStateReducer.reduce(null, 100, 200, false, 0).levelPercent)
        assertEquals(100, BatteryStateReducer.reduce(null, 300, 100, false, 0).levelPercent)
        assertEquals(40, BatteryStateReducer.reduce(null, 40, 0, false, 0).levelPercent)
    }
}
