package com.example.applock.util

import android.content.Context
import android.content.SharedPreferences

object PrefsHelper {
    private const val PREF_NAME = "app_lock_prefs"
    private const val KEY_PIN = "user_pin"
    private const val KEY_LOCKED_APPS = "locked_apps"
    private const val KEY_BIOMETRIC = "biometric_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isPinSet(context: Context): Boolean {
        return getPrefs(context).getString(KEY_PIN, null) != null
    }

    fun setPin(context: Context, pin: String) {
        getPrefs(context).edit().putString(KEY_PIN, pin).apply()
    }

    fun getPin(context: Context): String? {
        return getPrefs(context).getString(KEY_PIN, null)
    }

    fun verifyPin(context: Context, pin: String): Boolean {
        return getPin(context) == pin
    }

    fun getLockedApps(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_LOCKED_APPS, emptySet()) ?: emptySet()
    }

    fun setLockedApps(context: Context, apps: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_LOCKED_APPS, apps).apply()
    }

    fun isAppLocked(context: Context, packageName: String): Boolean {
        return getLockedApps(context).contains(packageName)
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC, enabled).apply()
    }
}
