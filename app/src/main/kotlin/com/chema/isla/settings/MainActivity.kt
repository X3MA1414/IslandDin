package com.chema.isla.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chema.isla.appContainer
import com.chema.isla.theme.IslaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SettingsViewModel by viewModels { SettingsViewModel.factory(appContainer) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val permissionsIntents = appContainer.permissionChecker
        setContent {
            IslaTheme {
                var showingApps by rememberSaveable { mutableStateOf(false) }
                val settings by viewModel.settings.collectAsStateWithLifecycle()
                val permissions by viewModel.permissions.collectAsStateWithLifecycle()
                val apps by viewModel.apps.collectAsStateWithLifecycle()

                BackHandler(enabled = showingApps) { showingApps = false }

                if (showingApps) {
                    AppPickerScreen(
                        apps = apps,
                        filterMode = settings?.appFilterMode,
                        onToggle = viewModel::toggleApp,
                        onBack = { showingApps = false },
                    )
                } else {
                    SettingsScreen(
                        settings = settings,
                        permissions = permissions,
                        onUpdate = viewModel::update,
                        onOpenAccessibility = { startActivity(permissionsIntents.accessibilitySettingsIntent()) },
                        onOpenNotificationAccess = { startActivity(permissionsIntents.notificationListenerSettingsIntent()) },
                        onOpenBattery = { startActivity(permissionsIntents.batteryOptimizationIntent()) },
                        onOpenAppDetails = { startActivity(permissionsIntents.appDetailsIntent()) },
                        onPickApps = {
                            viewModel.loadApps()
                            showingApps = true
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissions()
    }
}
