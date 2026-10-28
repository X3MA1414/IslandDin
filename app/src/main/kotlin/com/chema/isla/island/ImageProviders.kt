package com.chema.isla.island

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap

/** Icono de una app por su paquete. */
fun interface AppIconProvider {
    fun icon(packageName: String): Bitmap?
}

/** Carátula de la canción actual por su clave. */
fun interface ArtworkProvider {
    fun artwork(key: String?): Bitmap?
}

class AndroidAppIconProvider(
    private val packageManager: PackageManager,
    private val sizePx: Int = 96,
) : AppIconProvider {

    private val cache = LruCache<String, Bitmap>(32)

    override fun icon(packageName: String): Bitmap? {
        cache.get(packageName)?.let { return it }
        return runCatching {
            packageManager.getApplicationIcon(packageName).toBitmap(sizePx, sizePx)
        }.getOrNull()?.also { cache.put(packageName, it) }
    }
}
