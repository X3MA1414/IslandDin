package com.chema.isla.domain.usecase

import com.chema.isla.domain.model.InstalledApp
import com.chema.isla.domain.model.SelectableApp
import com.chema.isla.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSelectableAppsUseCaseTest {

    private val repo = object : InstalledAppsRepository {
        override suspend fun launchableApps() = listOf(
            InstalledApp("org.telegram", "telegram"),
            InstalledApp("com.chema.isla", "Isla"),
            InstalledApp("com.whatsapp", "WhatsApp"),
            InstalledApp("com.whatsapp", "WhatsApp"),
            InstalledApp("com.android.chrome", "Chrome"),
        )
    }

    @Test
    fun `sorts alphabetically, removes own app and duplicates and marks selection`() = runTest {
        val result = GetSelectableAppsUseCase(repo, "com.chema.isla")(selected = setOf("com.whatsapp"))
        assertEquals(
            listOf(
                SelectableApp(InstalledApp("com.android.chrome", "Chrome"), false),
                SelectableApp(InstalledApp("org.telegram", "telegram"), false),
                SelectableApp(InstalledApp("com.whatsapp", "WhatsApp"), true),
            ),
            result,
        )
    }
}
