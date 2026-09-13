package com.example.applock.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.applock.ui.LockScreenActivity
import com.example.applock.util.PrefsHelper

class AppLockAccessibilityService : AccessibilityService() {

    companion object {
        const val EXTRA_TARGET_PACKAGE = "TARGET_PACKAGE"
    }

    private var lastPackageName: String? = null

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
