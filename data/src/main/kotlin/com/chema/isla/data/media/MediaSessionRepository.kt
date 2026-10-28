package com.chema.isla.data.media

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import com.chema.isla.domain.model.MediaInfo
import com.chema.isla.domain.port.MediaCommands
import com.chema.isla.domain.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sigue la sesión multimedia activa del sistema.
 * Requiere que el listener de notificaciones esté habilitado, ya que Android
 * usa ese permiso para dar acceso a las sesiones de otras apps.
 */
class MediaSessionRepository(
    context: Context,
    private val artworkStore: ArtworkStore,
) : MediaRepository, MediaCommands {

    private val sessionManager = context.getSystemService(MediaSessionManager::class.java)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val state = MutableStateFlow<MediaInfo?>(null)
    override val media: StateFlow<MediaInfo?> = state.asStateFlow()

    private var listenerComponent: ComponentName? = null
    private var controller: MediaController? = null

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        selectController(controllers.orEmpty())
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() {
            detachController()
            publish()
        }
    }

    /** Se llama cuando el listener de notificaciones se conecta. */
    fun start(notificationListener: ComponentName) {
        listenerComponent = notificationListener
        runCatching {
            sessionManager.addOnActiveSessionsChangedListener(sessionsListener, notificationListener, mainHandler)
            selectController(sessionManager.getActiveSessions(notificationListener))
        }
    }

    fun stop() {
        runCatching { sessionManager.removeOnActiveSessionsChangedListener(sessionsListener) }
        detachController()
        state.value = null
        artworkStore.clear()
    }

    /** Vuelve a leer la posición de reproducción (para la barra de progreso). */
    fun refreshPosition() = publish()

    private fun selectController(controllers: List<MediaController>) {
        val preferred = controllers.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: controllers.firstOrNull()
        if (preferred?.sessionToken == controller?.sessionToken) {
            publish()
            return
        }
        detachController()
        controller = preferred?.also { it.registerCallback(controllerCallback, mainHandler) }
        publish()
    }

    private fun detachController() {
        controller?.unregisterCallback(controllerCallback)
        controller = null
    }

    private fun publish() {
        val c = controller
        if (c == null) {
            state.value = null
            return
        }
        val metadata = c.metadata
        val playback = c.playbackState
        val artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
        val info = MediaMapper.map(
            RawMedia(
                packageName = c.packageName,
                title = metadata?.getText(MediaMetadata.METADATA_KEY_TITLE),
                artist = metadata?.getText(MediaMetadata.METADATA_KEY_ARTIST),
                album = metadata?.getText(MediaMetadata.METADATA_KEY_ALBUM),
                isPlaying = playback?.state == PlaybackState.STATE_PLAYING,
                durationMillis = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
                positionMillis = playback?.let(::currentPosition) ?: 0L,
                hasArtwork = artwork != null,
            ),
        )
        if (info?.artworkKey != null && artwork != null) artworkStore.put(info.artworkKey!!, artwork)
        state.value = info
    }

    private fun currentPosition(state: PlaybackState): Long {
        if (state.state != PlaybackState.STATE_PLAYING) return state.position
        val elapsed = android.os.SystemClock.elapsedRealtime() - state.lastPositionUpdateTime
        return state.position + (elapsed * state.playbackSpeed).toLong()
    }

    override fun play() { controller?.transportControls?.play() }
    override fun pause() { controller?.transportControls?.pause() }
    override fun next() { controller?.transportControls?.skipToNext() }
    override fun previous() { controller?.transportControls?.skipToPrevious() }
}
