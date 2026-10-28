package com.chema.isla.overlay

/** Rectángulo de la cámara frontal en píxeles. */
data class CutoutBounds(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val centerX: Int get() = (left + right) / 2
    val centerY: Int get() = (top + bottom) / 2
}

/** Posición de la ventana: x relativo al centro de la pantalla, y desde arriba. */
data class IslandPosition(val xPx: Int, val yPx: Int)

/**
 * Calcula dónde colocar la isla para que quede centrada sobre la cámara.
 * Sin muesca detectada, se centra horizontalmente a [fallbackTopPx] del borde.
 */
object IslandPositionCalculator {

    fun compute(
        cutout: CutoutBounds?,
        screenWidthPx: Int,
        density: Float,
        pillHeightDp: Int,
        offsetXDp: Int,
        offsetYDp: Int,
        fallbackTopPx: Int,
    ): IslandPosition {
        val pillHeightPx = (pillHeightDp * density).toInt()
        val centerX = cutout?.centerX ?: (screenWidthPx / 2)
        val top = if (cutout != null) cutout.centerY - pillHeightPx / 2 else fallbackTopPx
        return IslandPosition(
            xPx = centerX - screenWidthPx / 2 + (offsetXDp * density).toInt(),
            yPx = (top + (offsetYDp * density).toInt()).coerceAtLeast(0),
        )
    }
}
