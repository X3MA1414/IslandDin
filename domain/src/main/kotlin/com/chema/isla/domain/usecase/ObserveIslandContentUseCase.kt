package com.chema.isla.domain.usecase

import com.chema.isla.domain.model.IslandContent
import com.chema.isla.domain.model.IslandSnapshot
import com.chema.isla.domain.policy.ContentPriorityPolicy
import com.chema.isla.domain.port.Clock
import com.chema.isla.domain.port.Ticker
import com.chema.isla.domain.repository.BatteryRepository
import com.chema.isla.domain.repository.MediaRepository
import com.chema.isla.domain.repository.NotificationRepository
import com.chema.isla.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged

/** Combina todas las fuentes y emite el contenido que la isla debe mostrar. */
class ObserveIslandContentUseCase(
    private val notificationRepository: NotificationRepository,
    private val mediaRepository: MediaRepository,
    private val batteryRepository: BatteryRepository,
    private val settingsRepository: SettingsRepository,
    private val policy: ContentPriorityPolicy,
    private val clock: Clock,
    private val ticker: Ticker,
) {
    operator fun invoke(): Flow<IslandContent> = combine(
        notificationRepository.notifications,
        mediaRepository.media,
        batteryRepository.status,
        settingsRepository.settings,
        ticker.ticks,
    ) { notifications, media, battery, settings, _ ->
        policy.select(IslandSnapshot(notifications, media, battery, settings), clock.nowMillis())
    }.distinctUntilChanged()
}
