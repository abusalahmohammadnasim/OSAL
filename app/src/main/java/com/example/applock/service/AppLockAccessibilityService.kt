package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.applock.ui.LockScreenActivity

class AppLockAccessibilityService : AccessibilityService() {

    private var lastPackageName: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        if (packageName == this.packageName) return

        val prefs = getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)

        val isAppLocked = prefs.getBoolean(packageName, false)
        val isSettingsOrInstaller = packageName == "com.android.settings" || 
                                   packageName == "com.google.android.packageinstaller" ||
                                   packageName == "com.android.packageinstaller"

        if (isAppLocked || isSettingsOrInstaller) {
            if (packageName != lastPackageName) {
                lastPackageName = packageName
                val intent = Intent(this, LockScreenActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra("TARGET_PACKAGE", packageName)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}
}
