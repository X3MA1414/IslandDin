package com.chema.isla.data.media

import android.graphics.Bitmap

/** Guarda la carátula actual. Solo se conserva una para no gastar memoria. */
class ArtworkStore {
    @Volatile
    private var current: Pair<String, Bitmap>? = null

    fun put(key: String, bitmap: Bitmap) {
        current = key to bitmap
    }

    fun get(key: String?): Bitmap? = current?.takeIf { it.first == key }?.second

    fun clear() {
        current = null
    }
}
