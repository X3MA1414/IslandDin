package com.chema.isla.domain.fakes

import com.chema.isla.domain.model.BatteryStatus
import com.chema.isla.domain.model.IslandNotification
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.MediaInfo
import com.chema.isla.domain.port.Clock
import com.chema.isla.domain.port.MediaCommands
import com.chema.isla.domain.port.NotificationActions
import com.chema.isla.domain.port.Ticker
import com.chema.isla.domain.repository.BatteryRepository
import com.chema.isla.domain.repository.MediaRepository
import com.chema.isla.domain.repository.NotificationRepository
import com.chema.isla.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNotificationRepository : NotificationRepository {
    override val notifications = MutableStateFlow<List<IslandNotification>>(emptyList())
}

class FakeMediaRepository : MediaRepository {
    override val media = MutableStateFlow<MediaInfo?>(null)
}

class FakeBatteryRepository : BatteryRepository {
    override val status = MutableStateFlow<BatteryStatus?>(null)
}

class FakeSettingsRepository(initial: IslandSettings = IslandSettings()) : SettingsRepository {
    val state = MutableStateFlow(initial)
    override val settings: Flow<IslandSettings> = state
    override suspend fun update(transform: (IslandSettings) -> IslandSettings) {
        state.value = transform(state.value)
    }
}

class FakeClock(var now: Long = 0L) : Clock {
    override fun nowMillis(): Long = now
}

/** Ticker manual: cada llamada a [tick] fuerza una reevaluación. */
class FakeTicker : Ticker {
    private val counter = MutableStateFlow(0)
    override val ticks: Flow<Unit> = counter.map { }

    fun tick() {
        counter.value += 1
    }
}

class RecordingMediaCommands : MediaCommands {
    val calls = mutableListOf<String>()
    override fun play() { calls += "play" }
    override fun pause() { calls += "pause" }
    override fun next() { calls += "next" }
    override fun previous() { calls += "previous" }
}

class RecordingNotificationActions(private val openResult: Boolean = true) : NotificationActions {
    val opened = mutableListOf<String>()
    val dismissed = mutableListOf<String>()
    override fun open(key: String): Boolean { opened += key; return openResult }
    override fun dismiss(key: String) { dismissed += key }
}

fun notification(
    key: String = "k",
    packageName: String = "com.whatsapp",
    title: String = "Ana",
    text: String = "Hola",
    postedAt: Long = 0L,
    isOngoing: Boolean = false,
    isGroupSummary: Boolean = false,
    isMediaStyle: Boolean = false,
) = IslandNotification(
    key = key,
    packageName = packageName,
    appLabel = "App",
    title = title,
    text = text,
    postedAtMillis = postedAt,
    isOngoing = isOngoing,
    isGroupSummary = isGroupSummary,
    isMediaStyle = isMediaStyle,
)

fun media(isPlaying: Boolean = true) = MediaInfo(
    packageName = "com.spotify.music",
    title = "Canción",
    artist = "Artista",
    isPlaying = isPlaying,
)
