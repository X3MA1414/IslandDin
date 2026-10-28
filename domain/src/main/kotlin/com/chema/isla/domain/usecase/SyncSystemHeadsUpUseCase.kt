package com.chema.isla.domain.usecase

import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.port.SystemHeadsUpController

/**
 * Oculta los avisos emergentes del sistema solo mientras la isla esté funcionando
 * y mostrando notificaciones. En cualquier otro caso los restaura, para que el
 * usuario nunca se quede sin avisos.
 */
class SyncSystemHeadsUpUseCase(private val controller: SystemHeadsUpController) {

    operator fun invoke(settings: IslandSettings, islandRunning: Boolean) {
        if (!controller.canControl) return
        val hide = islandRunning &&
            settings.enabled &&
            settings.showNotifications &&
            settings.hideSystemHeadsUp
        controller.setSystemHeadsUpEnabled(!hide)
    }
}
