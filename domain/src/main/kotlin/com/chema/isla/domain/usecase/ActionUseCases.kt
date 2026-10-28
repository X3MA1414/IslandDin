package com.chema.isla.domain.usecase

import com.chema.isla.domain.model.MediaAction
import com.chema.isla.domain.port.MediaCommands
import com.chema.isla.domain.port.NotificationActions
import com.chema.isla.domain.repository.MediaRepository

/** Ejecuta una acción de reproducción sobre la sesión activa. */
class ControlMediaUseCase(
    private val mediaRepository: MediaRepository,
    private val commands: MediaCommands,
) {
    operator fun invoke(action: MediaAction) {
        val media = mediaRepository.media.value ?: return
        when (action) {
            MediaAction.PLAY_PAUSE -> if (media.isPlaying) commands.pause() else commands.play()
            MediaAction.NEXT -> commands.next()
            MediaAction.PREVIOUS -> commands.previous()
        }
    }
}

class OpenNotificationUseCase(private val actions: NotificationActions) {
    operator fun invoke(key: String): Boolean = actions.open(key)
}

class DismissNotificationUseCase(private val actions: NotificationActions) {
    operator fun invoke(key: String) = actions.dismiss(key)
}
