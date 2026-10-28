package com.chema.isla.island

import com.chema.isla.domain.model.IslandContent
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.MediaAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Lógica de presentación de la isla, independiente de Android para poder testearla.
 * Gestiona la expansión manual del reproductor y su cierre automático.
 */
class IslandPresenter(
    content: Flow<IslandContent>,
    settings: Flow<IslandSettings>,
    private val controlMedia: (MediaAction) -> Unit,
    private val scope: CoroutineScope,
    private val mapper: IslandUiMapper = IslandUiMapper(),
    private val autoCollapseMillis: Long = AUTO_COLLAPSE_MILLIS,
) {
    private val mediaExpanded = MutableStateFlow(false)
    private val landscape = MutableStateFlow(false)
    private var collapseJob: Job? = null

    private val latestContent: StateFlow<IslandContent> =
        content.stateIn(scope, SharingStarted.Eagerly, IslandContent.Hidden)

    val uiState: StateFlow<IslandUiState> = combine(
        latestContent,
        settings,
        mediaExpanded,
        landscape,
    ) { c, s, expanded, isLandscape ->
        mapper.map(c, s, expanded, isLandscape)
    }.stateIn(scope, SharingStarted.Eagerly, IslandUiState.Hidden)

    init {
        // Si deja de haber música, el reproductor expandido se cierra.
        latestContent
            .map { it is IslandContent.Media }
            .distinctUntilChanged()
            .onEach { isMedia -> if (!isMedia) collapse() }
            .launchIn(scope)
    }

    fun setLandscape(isLandscape: Boolean) {
        landscape.value = isLandscape
    }

    fun onTap(): IslandTapResult = when (val c = latestContent.value) {
        is IslandContent.Notification -> IslandTapResult.OpenNotification(c.notification.key)
        is IslandContent.Media -> {
            if (mediaExpanded.value) collapse() else expand()
            IslandTapResult.None
        }
        else -> IslandTapResult.None
    }

    /** Pulsación larga: abre la app de la música. */
    fun onLongPress(): IslandTapResult = when (val c = latestContent.value) {
        is IslandContent.Media -> {
            collapse()
            IslandTapResult.OpenApp(c.media.packageName)
        }
        is IslandContent.Notification -> IslandTapResult.OpenNotification(c.notification.key)
        else -> IslandTapResult.None
    }

    fun onMediaAction(action: MediaAction) {
        controlMedia(action)
        if (mediaExpanded.value) scheduleCollapse()
    }

    private fun expand() {
        mediaExpanded.value = true
        scheduleCollapse()
    }

    private fun collapse() {
        collapseJob?.cancel()
        mediaExpanded.value = false
    }

    private fun scheduleCollapse() {
        collapseJob?.cancel()
        collapseJob = scope.launch {
            delay(autoCollapseMillis)
            mediaExpanded.value = false
        }
    }

    companion object {
        const val AUTO_COLLAPSE_MILLIS = 6_000L
    }
}
