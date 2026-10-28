package com.chema.isla.domain.usecase

import com.chema.isla.domain.fakes.FakeBatteryRepository
import com.chema.isla.domain.fakes.FakeClock
import com.chema.isla.domain.fakes.FakeMediaRepository
import com.chema.isla.domain.fakes.FakeNotificationRepository
import com.chema.isla.domain.fakes.FakeSettingsRepository
import com.chema.isla.domain.fakes.FakeTicker
import com.chema.isla.domain.fakes.media
import com.chema.isla.domain.fakes.notification
import com.chema.isla.domain.model.IslandContent
import com.chema.isla.domain.policy.DefaultContentPriorityPolicy
import com.chema.isla.domain.policy.DefaultNotificationFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveIslandContentUseCaseTest {

    private val notifications = FakeNotificationRepository()
    private val mediaRepo = FakeMediaRepository()
    private val battery = FakeBatteryRepository()
    private val settings = FakeSettingsRepository()
    private val clock = FakeClock(now = 0)
    private val ticker = FakeTicker()

    private val useCase = ObserveIslandContentUseCase(
        notificationRepository = notifications,
        mediaRepository = mediaRepo,
        batteryRepository = battery,
        settingsRepository = settings,
        policy = DefaultContentPriorityPolicy(DefaultNotificationFilter("com.chema.isla")),
        clock = clock,
        ticker = ticker,
    )

    @Test
    fun `emits content changes and expires notifications on tick`() = runTest {
        val collected = mutableListOf<IslandContent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase().collect { collected += it }
        }

        val m = media()
        mediaRepo.media.value = m
        val n = notification(postedAt = 100)
        clock.now = 200
        notifications.notifications.value = listOf(n)
        clock.now = 100 + settings.state.value.notificationDurationMillis
        ticker.tick()

        assertEquals(
            listOf(
                IslandContent.Idle,
                IslandContent.Media(m),
                IslandContent.Notification(n),
                IslandContent.Media(m),
            ),
            collected,
        )
    }

    @Test
    fun `identical evaluations are not emitted twice`() = runTest {
        val collected = mutableListOf<IslandContent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase().collect { collected += it }
        }

        repeat(5) { ticker.tick() }

        assertEquals(listOf<IslandContent>(IslandContent.Idle), collected)
    }

    @Test
    fun `disabling the island hides it`() = runTest {
        val collected = mutableListOf<IslandContent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            useCase().collect { collected += it }
        }

        settings.update { it.copy(enabled = false) }

        assertEquals(IslandContent.Hidden, collected.last())
    }
}
