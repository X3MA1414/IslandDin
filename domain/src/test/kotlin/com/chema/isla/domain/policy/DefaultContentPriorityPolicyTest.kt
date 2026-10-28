package com.chema.isla.domain.policy

import com.chema.isla.domain.fakes.media
import com.chema.isla.domain.fakes.notification
import com.chema.isla.domain.model.BatteryStatus
import com.chema.isla.domain.model.IslandContent
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.IslandSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultContentPriorityPolicyTest {

    private val policy = DefaultContentPriorityPolicy(
        notificationFilter = { n, _ -> !n.isOngoing },
    )
    private val settings = IslandSettings()

    private fun snapshot(
        notifications: List<com.chema.isla.domain.model.IslandNotification> = emptyList(),
        media: com.chema.isla.domain.model.MediaInfo? = null,
        battery: BatteryStatus? = null,
        settings: IslandSettings = this.settings,
    ) = IslandSnapshot(notifications, media, battery, settings)

    @Test
    fun `nothing active yields idle`() {
        assertEquals(IslandContent.Idle, policy.select(snapshot(), nowMillis = 10_000))
    }

    @Test
    fun `disabled island is hidden even with content`() {
        val result = policy.select(
            snapshot(media = media(), settings = settings.copy(enabled = false)),
            nowMillis = 0,
        )
        assertEquals(IslandContent.Hidden, result)
    }

    @Test
    fun `playing media is shown`() {
        val m = media()
        assertEquals(IslandContent.Media(m), policy.select(snapshot(media = m), 0))
    }

    @Test
    fun `paused media is not shown`() {
        assertEquals(IslandContent.Idle, policy.select(snapshot(media = media(isPlaying = false)), 0))
    }

    @Test
    fun `fresh notification beats media`() {
        val n = notification(postedAt = 1_000)
        val result = policy.select(snapshot(listOf(n), media = media()), nowMillis = 2_000)
        assertEquals(IslandContent.Notification(n), result)
    }

    @Test
    fun `expired notification falls back to media`() {
        val n = notification(postedAt = 0)
        val m = media()
        val now = settings.notificationDurationMillis
        assertEquals(IslandContent.Media(m), policy.select(snapshot(listOf(n), media = m), now))
    }

    @Test
    fun `most recent allowed notification wins`() {
        val old = notification(key = "old", postedAt = 100)
        val recent = notification(key = "new", postedAt = 200)
        val ongoing = notification(key = "ongoing", postedAt = 300, isOngoing = true)
        val result = policy.select(snapshot(listOf(old, recent, ongoing)), nowMillis = 500)
        assertEquals(IslandContent.Notification(recent), result)
    }

    @Test
    fun `fresh charging event beats everything`() {
        val battery = BatteryStatus(levelPercent = 50, isCharging = true, chargingChangedAtMillis = 1_000)
        val result = policy.select(
            snapshot(listOf(notification(postedAt = 1_500)), media = media(), battery = battery),
            nowMillis = 2_000,
        )
        assertEquals(IslandContent.Charging(battery), result)
    }

    @Test
    fun `charging event expires`() {
        val battery = BatteryStatus(50, true, chargingChangedAtMillis = 0)
        val result = policy.select(snapshot(battery = battery), nowMillis = settings.chargingDurationMillis)
        assertEquals(IslandContent.Idle, result)
    }

    @Test
    fun `disabled sources are ignored`() {
        val off = settings.copy(showNotifications = false, showMedia = false, showCharging = false)
        val result = policy.select(
            snapshot(
                notifications = listOf(notification(postedAt = 0)),
                media = media(),
                battery = BatteryStatus(10, true, 0),
                settings = off,
            ),
            nowMillis = 1,
        )
        assertEquals(IslandContent.Idle, result)
    }

    @Test
    fun `events from the future are not considered fresh`() {
        val n = notification(postedAt = 5_000)
        assertEquals(IslandContent.Idle, policy.select(snapshot(listOf(n)), nowMillis = 1_000))
    }
}
