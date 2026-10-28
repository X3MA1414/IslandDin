package com.chema.isla.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.chema.isla.domain.model.IslandSettings
import com.chema.isla.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val settings: Flow<IslandSettings> = dataStore.data
        .map(SettingsPreferencesMapper::fromPreferences)
        .distinctUntilChanged()

    override suspend fun update(transform: (IslandSettings) -> IslandSettings) {
        dataStore.edit { prefs ->
            val updated = transform(SettingsPreferencesMapper.fromPreferences(prefs))
            SettingsPreferencesMapper.write(updated, prefs)
        }
    }
}
