package com.example.applock.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout

/**
 * A bare opaque view added directly to the window manager (not an Activity).
 * This has none of the startup cost of launching an Activity, so it can go up
 * essentially instantly the moment a locked app is detected in the foreground
 * — closing the gap where the real app's content is briefly visible/touchable
 * before LockScreenActivity finishes launching on top of it.
 *
 * It intercepts all touches (so scrolling/tapping the app underneath is not
 * possible) and is removed once the real lock screen has taken over, or if
 * something goes wrong, after a safety timeout.
 */
object OverlayBlocker {

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    fun hasPermission(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun show(context: Context) {
        if (overlayView != null) return // already showing
        if (!hasPermission(context)) return

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val view = FrameLayout(context).apply {
            setBackgroundColor(0xFF101418.toInt())
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.OPAQUE
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        try {
            wm.addView(view, params)
            windowManager = wm
            overlayView = view
        } catch (e: Exception) {
            // If the overlay can't be added for any reason, fail silently —
            // LockScreenActivity is still launched separately and remains
            // the primary defense.
        }
    }

    fun hide() {
        try {
            overlayView?.let { windowManager?.removeView(it) }
        } catch (e: Exception) {
            // View already removed or window gone; nothing to do.
        }
        overlayView = null
        windowManager = null
    }
}
