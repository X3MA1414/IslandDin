package com.chema.isla.domain.usecase

import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveSettingsUseCase(private val repository: SettingsRepository) {
    operator fun invoke(): Flow<IslandSettings> = repository.settings.map { it.normalized() }
}

/** Aplica un cambio a los ajustes garantizando que el resultado es válido. */
class UpdateSettingsUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(transform: (IslandSettings) -> IslandSettings) {
        repository.update { current -> transform(current).normalized() }
    }
}

/** Añade o quita una app de la lista seleccionada. */
class ToggleAppSelectionUseCase(private val updateSettings: UpdateSettingsUseCase) {
    suspend operator fun invoke(packageName: String) {
        updateSettings { settings ->
            val selected = settings.selectedPackages
            settings.copy(
                selectedPackages = if (packageName in selected) selected - packageName
                else selected + packageName,
            )
        }
    }
}
