package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.example.applock.ui.LockScreenActivity
import com.example.applock.util.PrefsHelper

class AppLockAccessibilityService : AccessibilityService() {

    // Package that is currently "unlocked" (user just entered the correct PIN for it).
    // It stays unlocked only while it remains the foreground app; the moment the
    // foreground app changes to something else, this is cleared, so returning to
    // the locked app later re-prompts for the PIN.
    private var unlockedPackage: String? = null
    private var ownPackage: String = ""
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlaySafetyRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        ownPackage = packageName
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        if (pkg == ownPackage) {
            // Our own lock screen has taken over the foreground — the real
            // UI is now covering everything, so the instant scrim is no
            // longer needed.
            clearOverlaySafety()
            OverlayBlocker.hide()
            return
        }

        if (pkg != unlockedPackage) {
            // Foreground app changed to something not currently unlocked.
            unlockedPackage = null
            if (PrefsHelper.isAppLocked(applicationContext, pkg)) {
                // Put the opaque blocker up FIRST — this is near-instant
                // (a plain WindowManager view, no Activity launch cost) and
                // blocks both the content flash and any touches/scrolling on
                // the app underneath while the real lock screen spins up.
                OverlayBlocker.show(applicationContext)
                armOverlaySafety()
                launchLockScreen(pkg)
            } else {
                OverlayBlocker.hide()
            }
        }
    }

    private fun launchLockScreen(targetPackage: String) {
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(LockScreenActivity.EXTRA_TARGET_PACKAGE, targetPackage)
        }
        startActivity(intent)
    }

    // If for any reason LockScreenActivity never reports itself in the
    // foreground (e.g. it was blocked by the OS), don't leave the user
    // staring at a permanent black screen — drop the overlay after a short
    // safety window.
    private fun armOverlaySafety() {
        clearOverlaySafety()
        val runnable = Runnable { OverlayBlocker.hide() }
        overlaySafetyRunnable = runnable
        mainHandler.postDelayed(runnable, 4000)
    }

    private fun clearOverlaySafety() {
        overlaySafetyRunnable?.let { mainHandler.removeCallbacks(it) }
        overlaySafetyRunnable = null
    }

    /** Called by LockScreenActivity after a correct PIN entry. */
    fun markUnlocked(pkg: String) {
        unlockedPackage = pkg
        clearOverlaySafety()
        OverlayBlocker.hide()
    }

    companion object {
        var instance: AppLockAccessibilityService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        clearOverlaySafety()
        OverlayBlocker.hide()
        instance = null
    }

    override fun onInterrupt() {}
}
