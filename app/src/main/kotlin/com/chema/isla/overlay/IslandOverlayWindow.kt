package com.chema.isla.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy

/**
 * Ventana flotante de tipo accesibilidad que se dibuja por encima de la barra de estado.
 * Solo se encarga de añadir, mover y quitar la ventana (responsabilidad única).
 */
class IslandOverlayWindow(
    private val context: Context,
    private val windowManager: WindowManager,
) {
    private var lifecycleOwner: OverlayLifecycleOwner? = null
    private var view: ComposeView? = null

    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        title = "Isla"
    }

    fun show(content: @Composable () -> Unit) {
        if (view != null) return
        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent(content)
        }
        val owner = OverlayLifecycleOwner().also {
            it.create()
            it.attachTo(composeView)
        }
        windowManager.addView(composeView, params)
        owner.resume()
        lifecycleOwner = owner
        view = composeView
    }

    fun moveTo(position: IslandPosition) {
        val v = view ?: return
        if (params.x == position.xPx && params.y == position.yPx) return
        params.x = position.xPx
        params.y = position.yPx
        windowManager.updateViewLayout(v, params)
    }

    fun remove() {
        val v = view ?: return
        lifecycleOwner?.destroy()
        lifecycleOwner = null
        runCatching { windowManager.removeView(v) }
        view = null
    }
}
