package com.chema.isla.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.res.Configuration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chema.isla.appContainer
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.MediaAction
import com.chema.isla.island.Island
import com.chema.isla.island.IslandCallbacks
import com.chema.isla.island.IslandPresenter
import com.chema.isla.island.IslandTapResult
import com.chema.isla.overlay.CutoutBounds
import com.chema.isla.overlay.IslandOverlayWindow
import com.chema.isla.overlay.IslandPositionCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Servicio de accesibilidad: solo se usa para poder dibujar la isla encima de la
 * barra de estado. No lee el contenido de la pantalla.
 */
class IslandAccessibilityService : AccessibilityService() {

    private var scope: CoroutineScope? = null
    private var overlay: IslandOverlayWindow? = null
    private var presenter: IslandPresenter? = null
    private var lastSettings: IslandSettings? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        val container = appContainer
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        scope = serviceScope

        container.batteryRepository.start()

        val islandPresenter = IslandPresenter(
            content = container.observeIslandContent(),
            settings = container.observeSettings(),
            controlMedia = { action -> container.controlMedia(action) },
            scope = serviceScope,
        ).also { it.setLandscape(isLandscape()) }
        presenter = islandPresenter

        val callbacks = object : IslandCallbacks {
            override fun onTap() = handle(islandPresenter.onTap())
            override fun onLongPress() = handle(islandPresenter.onLongPress())
            override fun onMediaAction(action: MediaAction) = islandPresenter.onMediaAction(action)
        }

        val window = IslandOverlayWindow(this, getSystemService(WindowManager::class.java))
        overlay = window
        window.show {
            val state by islandPresenter.uiState.collectAsStateWithLifecycle()
            Island(state, container.appIconProvider, container.artworkProvider, callbacks)
        }

        container.observeSettings()
            .onEach { settings ->
                lastSettings = settings
                reposition(settings)
                container.syncSystemHeadsUp(settings, islandRunning = true)
            }
            .launchIn(serviceScope)
    }

    private fun handle(result: IslandTapResult) {
        when (result) {
            IslandTapResult.None -> Unit
            is IslandTapResult.OpenNotification -> appContainer.openNotification(result.key)
            is IslandTapResult.OpenApp -> packageManager.getLaunchIntentForPackage(result.packageName)
                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ?.let(::startActivity)
        }
    }

    private fun reposition(settings: IslandSettings) {
        val wm = getSystemService(WindowManager::class.java)
        val metrics = wm.currentWindowMetrics
        val cutoutRect = metrics.windowInsets.displayCutout?.boundingRectTop?.takeIf { !it.isEmpty }
        val density = resources.displayMetrics.density
        val position = IslandPositionCalculator.compute(
            cutout = cutoutRect?.let { CutoutBounds(it.left, it.top, it.right, it.bottom) },
            screenWidthPx = metrics.bounds.width(),
            density = density,
            pillHeightDp = settings.pillHeightDp,
            offsetXDp = settings.offsetXDp,
            offsetYDp = settings.offsetYDp,
            fallbackTopPx = (8 * density).toInt(),
        )
        overlay?.moveTo(position)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        presenter?.setLandscape(isLandscape())
        lastSettings?.let(::reposition)
    }

    private fun isLandscape() =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        // Nunca dejar al usuario sin avisos si la isla se detiene.
        lastSettings?.let { appContainer.syncSystemHeadsUp(it, islandRunning = false) }
        overlay?.remove()
        overlay = null
        presenter = null
        scope?.cancel()
        scope = null
        appContainer.batteryRepository.stop()
        super.onDestroy()
    }
}
