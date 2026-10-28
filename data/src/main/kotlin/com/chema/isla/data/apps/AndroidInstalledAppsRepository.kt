package com.chema.isla.data.apps

import android.content.Intent
import android.content.pm.PackageManager
import com.chema.isla.domain.model.InstalledApp
import com.chema.isla.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidInstalledAppsRepository(
    private val packageManager: PackageManager,
) : InstalledAppsRepository {

    override suspend fun launchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            .map { info ->
                InstalledApp(
                    packageName = info.activityInfo.packageName,
                    label = info.loadLabel(packageManager).toString(),
                )
            }
    }
}
