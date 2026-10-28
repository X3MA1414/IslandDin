package com.chema.isla.domain.model

data class InstalledApp(val packageName: String, val label: String)

data class SelectableApp(val app: InstalledApp, val isSelected: Boolean)
