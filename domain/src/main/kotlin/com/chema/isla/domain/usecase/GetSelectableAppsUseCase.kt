package com.chema.isla.domain.usecase

import com.chema.isla.domain.model.InstalledApp
import com.chema.isla.domain.model.SelectableApp
import com.chema.isla.domain.repository.InstalledAppsRepository

/** Lista de apps ordenada alfabéticamente, marcando las seleccionadas y excluyendo la propia. */
class GetSelectableAppsUseCase(
    private val repository: InstalledAppsRepository,
    private val ownPackageName: String,
) {
    suspend operator fun invoke(selected: Set<String>): List<SelectableApp> =
        repository.launchableApps()
            .asSequence()
            .filter { it.packageName != ownPackageName }
            .distinctBy(InstalledApp::packageName)
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, InstalledApp::label))
            .map { SelectableApp(it, it.packageName in selected) }
            .toList()
}
