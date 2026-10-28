package com.chema.isla.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.chema.isla.data.apps.AndroidInstalledAppsRepository
import com.chema.isla.data.battery.AndroidBatteryRepository
import com.chema.isla.data.media.ArtworkStore
import com.chema.isla.data.media.MediaSessionRepository
import com.chema.isla.data.notification.AndroidNotificationActions
import com.chema.isla.data.notification.AndroidNotificationExtractor
import com.chema.isla.data.notification.InMemoryNotificationRepository
import com.chema.isla.data.settings.DataStoreSettingsRepository
import com.chema.isla.data.system.AndroidHeadsUpController
import com.chema.isla.data.time.IntervalTicker
import com.chema.isla.data.time.SystemClock
import com.chema.isla.domain.policy.DefaultContentPriorityPolicy
import com.chema.isla.domain.policy.DefaultNotificationFilter
import com.chema.isla.domain.usecase.ControlMediaUseCase
import com.chema.isla.domain.usecase.DismissNotificationUseCase
import com.chema.isla.domain.usecase.GetSelectableAppsUseCase
import com.chema.isla.domain.usecase.ObserveIslandContentUseCase
import com.chema.isla.domain.usecase.ObserveSettingsUseCase
import com.chema.isla.domain.usecase.OpenNotificationUseCase
import com.chema.isla.domain.usecase.SyncSystemHeadsUpUseCase
import com.chema.isla.domain.usecase.ToggleAppSelectionUseCase
import com.chema.isla.domain.usecase.UpdateSettingsUseCase
import com.chema.isla.island.AndroidAppIconProvider
import com.chema.isla.island.AppIconProvider
import com.chema.isla.island.ArtworkProvider
import com.chema.isla.permissions.AndroidPermissionChecker
import com.chema.isla.permissions.PermissionChecker

private val Context.settingsDataStore by preferencesDataStore(name = "island_settings")

/**
 * Composition root: el único lugar que conoce las implementaciones concretas.
 * El resto del código depende solo de interfaces del dominio (inversión de dependencias).
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val ownPackage = appContext.packageName

    // Datos
    val clock = SystemClock
    private val settingsRepository = DataStoreSettingsRepository(appContext.settingsDataStore)
    private val notificationRepository = InMemoryNotificationRepository()
    private val artworkStore = ArtworkStore()
    val mediaRepository = MediaSessionRepository(appContext, artworkStore)
    val batteryRepository = AndroidBatteryRepository(appContext, clock)
    val notificationActions = AndroidNotificationActions(appContext)
    private val headsUpController = AndroidHeadsUpController(appContext)
    private val installedAppsRepository = AndroidInstalledAppsRepository(appContext.packageManager)

    /** Solo el listener de notificaciones escribe aquí. */
    val notificationSink = notificationRepository
    val notificationExtractor = AndroidNotificationExtractor(appContext.packageManager)

    // Presentación: proveedores de imágenes
    val appIconProvider: AppIconProvider = AndroidAppIconProvider(appContext.packageManager)
    val artworkProvider = ArtworkProvider { key -> artworkStore.get(key) }

    val permissionChecker: PermissionChecker = AndroidPermissionChecker(appContext, headsUpController)

    // Casos de uso
    val observeSettings = ObserveSettingsUseCase(settingsRepository)
    val updateSettings = UpdateSettingsUseCase(settingsRepository)
    val toggleAppSelection = ToggleAppSelectionUseCase(updateSettings)
    val getSelectableApps = GetSelectableAppsUseCase(installedAppsRepository, ownPackage)
    val controlMedia = ControlMediaUseCase(mediaRepository, mediaRepository)
    val openNotification = OpenNotificationUseCase(notificationActions)
    val dismissNotification = DismissNotificationUseCase(notificationActions)
    val syncSystemHeadsUp = SyncSystemHeadsUpUseCase(headsUpController)
    val observeIslandContent = ObserveIslandContentUseCase(
        notificationRepository = notificationRepository,
        mediaRepository = mediaRepository,
        batteryRepository = batteryRepository,
        settingsRepository = settingsRepository,
        policy = DefaultContentPriorityPolicy(DefaultNotificationFilter(ownPackage)),
        clock = clock,
        ticker = IntervalTicker(periodMillis = 500L),
    )
}
