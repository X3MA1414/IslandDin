package com.chema.isla.domain.usecase

import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.port.SystemHeadsUpController
import org.junit.Assert.assertEquals
import org.junit.Test

class SyncSystemHeadsUpUseCaseTest {

    private class FakeController(override val canControl: Boolean = true) : SystemHeadsUpController {
        val calls = mutableListOf<Boolean>()
        override fun setSystemHeadsUpEnabled(enabled: Boolean) { calls += enabled }
    }

    private val hiding = IslandSettings(hideSystemHeadsUp = true)

    @Test
    fun `hides system pop-ups while the island is running`() {
        val c = FakeController()
        SyncSystemHeadsUpUseCase(c)(hiding, islandRunning = true)
        assertEquals(listOf(false), c.calls)
    }

    @Test
    fun `restores system pop-ups when the island stops`() {
        val c = FakeController()
        SyncSystemHeadsUpUseCase(c)(hiding, islandRunning = false)
        assertEquals(listOf(true), c.calls)
    }

    @Test
    fun `restores them when the option, notifications or the island are off`() {
        val c = FakeController()
        val sync = SyncSystemHeadsUpUseCase(c)
        sync(hiding.copy(hideSystemHeadsUp = false), true)
        sync(hiding.copy(showNotifications = false), true)
        sync(hiding.copy(enabled = false), true)
        assertEquals(listOf(true, true, true), c.calls)
    }

    @Test
    fun `does nothing without permission`() {
        val c = FakeController(canControl = false)
        SyncSystemHeadsUpUseCase(c)(hiding, true)
        assertEquals(emptyList<Boolean>(), c.calls)
    }
}
