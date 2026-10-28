package com.chema.isla.domain.model

enum class AppFilterMode {
    /** Se muestran todas las apps excepto las seleccionadas. */
    ALL_EXCEPT_SELECTED,

    /** Solo se muestran las apps seleccionadas. */
    ONLY_SELECTED,
}

/** Preferencias del usuario. Usa [normalized] para garantizar valores dentro de rango. */
data class IslandSettings(
    val enabled: Boolean = true,
    val showNotifications: Boolean = true,
    val showMedia: Boolean = true,
    val showCharging: Boolean = true,
    val showIdlePill: Boolean = true,
    val hideInLandscape: Boolean = true,
    /** Desactiva los avisos emergentes del sistema para que solo se vea la isla. */
    val hideSystemHeadsUp: Boolean = false,
    val notificationDurationMillis: Long = DEFAULT_NOTIFICATION_DURATION,
    val chargingDurationMillis: Long = DEFAULT_CHARGING_DURATION,
    val pillWidthDp: Int = DEFAULT_PILL_WIDTH,
    val pillHeightDp: Int = DEFAULT_PILL_HEIGHT,
    val expandedWidthDp: Int = DEFAULT_EXPANDED_WIDTH,
    val offsetXDp: Int = 0,
    val offsetYDp: Int = 0,
    val accentColorArgb: Long = DEFAULT_ACCENT,
    val appFilterMode: AppFilterMode = AppFilterMode.ALL_EXCEPT_SELECTED,
    val selectedPackages: Set<String> = emptySet(),
) {
    fun normalized(): IslandSettings = copy(
        notificationDurationMillis = notificationDurationMillis.coerceIn(DURATION_RANGE),
        chargingDurationMillis = chargingDurationMillis.coerceIn(DURATION_RANGE),
        pillWidthDp = pillWidthDp.coerceIn(PILL_WIDTH_RANGE),
        pillHeightDp = pillHeightDp.coerceIn(PILL_HEIGHT_RANGE),
        expandedWidthDp = expandedWidthDp.coerceIn(EXPANDED_WIDTH_RANGE)
            .coerceAtLeast(pillWidthDp.coerceIn(PILL_WIDTH_RANGE)),
        offsetXDp = offsetXDp.coerceIn(OFFSET_RANGE),
        offsetYDp = offsetYDp.coerceIn(OFFSET_RANGE),
    )

    companion object {
        const val DEFAULT_NOTIFICATION_DURATION = 4_000L
        const val DEFAULT_CHARGING_DURATION = 3_000L
        const val DEFAULT_PILL_WIDTH = 96
        const val DEFAULT_PILL_HEIGHT = 32
        const val DEFAULT_EXPANDED_WIDTH = 360
        const val DEFAULT_ACCENT = 0xFF34C759L

        val DURATION_RANGE = 1_000L..15_000L
        val PILL_WIDTH_RANGE = 40..200
        val PILL_HEIGHT_RANGE = 20..60
        val EXPANDED_WIDTH_RANGE = 200..420
        val OFFSET_RANGE = -60..60
    }
}
