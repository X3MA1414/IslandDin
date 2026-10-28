package com.chema.isla.domain.usecase

import com.chema.isla.domain.fakes.FakeMediaRepository
import com.chema.isla.domain.fakes.FakeSettingsRepository
import com.chema.isla.domain.fakes.RecordingMediaCommands
import com.chema.isla.domain.fakes.RecordingNotificationActions
import com.chema.isla.domain.fakes.media
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.MediaAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsAndActionUseCasesTest {

    @Test
    fun `update settings normalizes invalid values`() = runTest {
        val repo = FakeSettingsRepository()
        UpdateSettingsUseCase(repo) { it.copy(pillWidthDp = 9_999) }
        assertEquals(IslandSettings.PILL_WIDTH_RANGE.last, repo.state.value.pillWidthDp)
    }

    @Test
    fun `observe settings emits normalized values`() = runTest {
        val repo = FakeSettingsRepository(IslandSettings(offsetYDp = -9_999))
        val observed = ObserveSettingsUseCase(repo)().first()
        assertEquals(IslandSettings.OFFSET_RANGE.first, observed.offsetYDp)
    }

    @Test
    fun `toggle app selection adds and removes`() = runTest {
        val repo = FakeSettingsRepository()
        val toggle = ToggleAppSelectionUseCase(UpdateSettingsUseCase(repo))

        toggle("com.whatsapp")
        assertEquals(setOf("com.whatsapp"), repo.state.value.selectedPackages)

        toggle("com.whatsapp")
        assertTrue(repo.state.value.selectedPackages.isEmpty())
    }

    @Test
    fun `play pause toggles depending on current state`() {
        val repo = FakeMediaRepository()
        val commands = RecordingMediaCommands()
        val control = ControlMediaUseCase(repo, commands)

        repo.media.value = media(isPlaying = true)
        control(MediaAction.PLAY_PAUSE)
        repo.media.value = media(isPlaying = false)
        control(MediaAction.PLAY_PAUSE)
        control(MediaAction.NEXT)
        control(MediaAction.PREVIOUS)

        assertEquals(listOf("pause", "play", "next", "previous"), commands.calls)
    }

    @Test
    fun `media actions are ignored without an active session`() {
        val commands = RecordingMediaCommands()
        ControlMediaUseCase(FakeMediaRepository(), commands)(MediaAction.NEXT)
        assertTrue(commands.calls.isEmpty())
    }

    @Test
    fun `notification actions are delegated`() {
        val actions = RecordingNotificationActions()
        assertTrue(OpenNotificationUseCase(actions)("a"))
        DismissNotificationUseCase(actions)("b")
        assertEquals(listOf("a"), actions.opened)
        assertEquals(listOf("b"), actions.dismissed)
    }
}
