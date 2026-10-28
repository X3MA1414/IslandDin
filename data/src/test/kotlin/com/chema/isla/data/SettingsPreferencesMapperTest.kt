package com.chema.isla.data

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.chema.isla.data.settings.SettingsPreferencesMapper
import com.chema.isla.domain.model.AppFilterMode
import com.chema.isla.domain.model.IslandSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsPreferencesMapperTest {

    @Test
    fun `empty preferences produce default settings`() {
        assertEquals(IslandSettings(), SettingsPreferencesMapper.fromPreferences(emptyPreferences()))
    }

    @Test
    fun `settings survive a write and read round trip`() {
        val settings = IslandSettings(
            enabled = false,
            showMedia = false,
            pillWidthDp = 120,
            offsetYDp = -4,
            accentColorArgb = 0xFF0A84FFL,
            appFilterMode = AppFilterMode.ONLY_SELECTED,
            selectedPackages = setOf("com.whatsapp", "org.telegram.messenger"),
        )
        val prefs = mutablePreferencesOf()
        SettingsPreferencesMapper.write(settings, prefs)
        assertEquals(settings, SettingsPreferencesMapper.fromPreferences(prefs))
    }
}
