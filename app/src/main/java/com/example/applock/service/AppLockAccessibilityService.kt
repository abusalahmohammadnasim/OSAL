package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.applock.ui.LockScreenActivity
import com.example.applock.util.PrefsHelper

class AppLockAccessibilityService : AccessibilityService() {

    private var unlockedPackage: String? = null
    private var ownPackage: String = ""
    private val mainHandler = Handler(Looper.getMainLooper())
    private var overlaySafetyRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        ownPackage = packageName
        startForegroundServiceNotification()
    }

    private fun startForegroundServiceNotification() {
        try {
            val channelId = "AppLockServiceChannel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "App Lock Protection Service",
                    NotificationManager.IMPORTANCE_LOW
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("App Lock Guard Active")
                .setContentText("Protecting your apps and system in background")
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .build()

            startForeground(1001, notification)
        } catch (e: Exception) {
            Log.e("AppLockService", "Failed to start foreground service: ${e.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            val pkg = event?.packageName?.toString() ?: return
            val className = event.className?.toString() ?: ""
            if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

            if (pkg == ownPackage) {
                clearOverlaySafety()
                safeHideOverlay()
                return
            }

            // আনইনস্টল করার চেষ্টা বা অ্যাপ রিমুভ করার পেজ ডিটেক্ট করা
            val isUninstallAttempt = pkg.contains("packageinstaller") || 
                                     (pkg.contains("settings") && className.contains("Uninstaller", ignoreCase = true))

            if (pkg != unlockedPackage) {
                unlockedPackage = null
                if (PrefsHelper.isAppLocked(applicationContext, pkg) || isUninstallAttempt) {
                    safeShowOverlay()
                    armOverlaySafety()
                    launchLockScreen(pkg)
                } else {
                    safeHideOverlay()
                }
            }
        } catch (e: Exception) {
            Log.e("AppLockService", "Error processing accessibility event: ${e.message}")
        }
    }

    private fun safeShowOverlay() {
        try {
            OverlayBlocker.show(applicationContext)
        } catch (e: Exception) {
            Log.e("AppLockService", "Failed to show overlay: ${e.message}")
        }
    }

    private fun safeHideOverlay() {
        try {
            OverlayBlocker.hide()
        } catch (e: Exception) {
            Log.e("AppLockService", "Failed to hide overlay: ${e.message}")
        }
    }

    private fun launchLockScreen(targetPackage: String) {
        try {
            val intent = Intent(this, LockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(LockScreenActivity.EXTRA_TARGET_PACKAGE, targetPackage)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("AppLockService", "Failed to launch LockScreenActivity: ${e.message}")
        }
    }

    private fun armOverlaySafety() {
        clearOverlaySafety()
        val runnable = Runnable { safeHideOverlay() }
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
        safeHideOverlay()
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
        safeHideOverlay()
        instance = null
    }

    override fun onInterrupt() {}
}
