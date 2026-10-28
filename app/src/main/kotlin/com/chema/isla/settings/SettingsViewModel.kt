package com.chema.isla.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chema.isla.di.AppContainer
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.model.SelectableApp
import com.chema.isla.domain.usecase.GetSelectableAppsUseCase
import com.chema.isla.domain.usecase.ObserveSettingsUseCase
import com.chema.isla.domain.usecase.ToggleAppSelectionUseCase
import com.chema.isla.domain.usecase.UpdateSettingsUseCase
import com.chema.isla.permissions.PermissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    observeSettings: ObserveSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
    private val toggleAppSelection: ToggleAppSelectionUseCase,
    private val getSelectableApps: GetSelectableAppsUseCase,
    private val permissionStatus: () -> PermissionStatus,
) : ViewModel() {

    val settings: StateFlow<IslandSettings?> =
        observeSettings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _permissions = MutableStateFlow(permissionStatus())
    val permissions: StateFlow<PermissionStatus> = _permissions.asStateFlow()

    private val _apps = MutableStateFlow<List<SelectableApp>?>(null)
    val apps: StateFlow<List<SelectableApp>?> = _apps.asStateFlow()

    fun refreshPermissions() {
        _permissions.value = permissionStatus()
    }

    fun update(transform: (IslandSettings) -> IslandSettings) {
        viewModelScope.launch { updateSettings(transform) }
    }

    fun loadApps() {
        viewModelScope.launch {
            val selected = settings.value?.selectedPackages.orEmpty()
            _apps.value = getSelectableApps(selected)
        }
    }

    fun toggleApp(packageName: String) {
        viewModelScope.launch {
            toggleAppSelection(packageName)
            _apps.value = _apps.value?.map {
                if (it.app.packageName == packageName) it.copy(isSelected = !it.isSelected) else it
            }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                SettingsViewModel(
                    observeSettings = container.observeSettings,
                    updateSettings = container.updateSettings,
                    toggleAppSelection = container.toggleAppSelection,
                    getSelectableApps = container.getSelectableApps,
                    permissionStatus = container.permissionChecker::status,
                )
            }
        }
    }
}
