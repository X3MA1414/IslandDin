package com.chema.isla

import com.chema.isla.domain.model.IslandContent
import com.chema.isla.domain.model.IslandNotification
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.MediaAction
import com.chema.isla.domain.model.MediaInfo
import com.chema.isla.island.IslandBody
import com.chema.isla.island.IslandPresenter
import com.chema.isla.island.IslandTapResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IslandPresenterTest {

    private val media = MediaInfo("com.spotify.music", "t", "a", isPlaying = true)
    private val notification = IslandNotification("key", "pkg", "App", "Hola", "", 0)
    private val content = MutableStateFlow<IslandContent>(IslandContent.Idle)
    private val settings = MutableStateFlow(IslandSettings())
    private val actions = mutableListOf<MediaAction>()

    private fun TestScope.presenter() = IslandPresenter(
        content = content,
        settings = settings,
        controlMedia = { actions += it },
        scope = backgroundScope,
        autoCollapseMillis = 1_000,
    )

    @Test
    fun `tapping media expands and tapping again collapses`() = runTest(UnconfinedTestDispatcher()) {
        val p = presenter()
        content.value = IslandContent.Media(media)
        assertTrue(p.uiState.value.body is IslandBody.MediaCompact)

        assertEquals(IslandTapResult.None, p.onTap())
        assertTrue(p.uiState.value.body is IslandBody.MediaExpanded)

        p.onTap()
        assertTrue(p.uiState.value.body is IslandBody.MediaCompact)
    }

    @Test
    fun `expanded player collapses automatically`() = runTest(UnconfinedTestDispatcher()) {
        val p = presenter()
        content.value = IslandContent.Media(media)
        p.onTap()
        advanceTimeBy(1_001)
        assertTrue(p.uiState.value.body is IslandBody.MediaCompact)
    }

    @Test
    fun `media actions are forwarded and postpone the collapse`() = runTest(UnconfinedTestDispatcher()) {
        val p = presenter()
        content.value = IslandContent.Media(media)
        p.onTap()
        advanceTimeBy(800)
        p.onMediaAction(MediaAction.NEXT)
        advanceTimeBy(800)
        assertTrue(p.uiState.value.body is IslandBody.MediaExpanded)
        assertEquals(listOf(MediaAction.NEXT), actions)
    }

    @Test
    fun `tapping a notification asks to open it`() = runTest(UnconfinedTestDispatcher()) {
        val p = presenter()
        content.value = IslandContent.Notification(notification)
        assertEquals(IslandTapResult.OpenNotification("key"), p.onTap())
    }

    @Test
    fun `long press on media opens the player app`() = runTest(UnconfinedTestDispatcher()) {
        val p = presenter()
        content.value = IslandContent.Media(media)
        assertEquals(IslandTapResult.OpenApp("com.spotify.music"), p.onLongPress())
    }

    @Test
    fun `expansion is reset when media stops being shown`() = runTest(UnconfinedTestDispatcher()) {
        val p = presenter()
        content.value = IslandContent.Media(media)
        p.onTap()
        content.value = IslandContent.Notification(notification)
        content.value = IslandContent.Media(media)
        assertTrue(p.uiState.value.body is IslandBody.MediaCompact)
    }

    @Test
    fun `tapping idle does nothing`() = runTest(UnconfinedTestDispatcher()) {
        assertEquals(IslandTapResult.None, presenter().onTap())
    }
}
