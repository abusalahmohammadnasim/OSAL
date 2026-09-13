package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.applock.ui.LockScreenActivity
import com.example.applock.util.PrefsHelper

class AppLockAccessibilityService : AccessibilityService() {

    companion object {
        const val EXTRA_TARGET_PACKAGE = "TARGET_PACKAGE"
        private const val CHANNEL_ID = "app_lock_service_channel"
        private const val NOTIF_ID = 1001
    }

    private var lastPackageName: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        startForegroundServiceNotification()
    }

    private fun startForegroundServiceNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AppLock Protection Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AppLock is Active")
            .setContentText("Protecting your locked applications in real-time.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notification)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName == this.packageName) return

        if (PrefsHelper.isAppLocked(this, packageName) && packageName != lastPackageName) {
            lastPackageName = packageName
            val intent = Intent(this, LockScreenActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra(EXTRA_TARGET_PACKAGE, packageName)
            }
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}
}
