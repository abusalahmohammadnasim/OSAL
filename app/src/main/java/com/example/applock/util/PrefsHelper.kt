package com.example.applock.util

import android.content.Context
import android.content.SharedPreferences

object PrefsHelper {
    private const val PREF_NAME = "app_lock_prefs"
    private const val KEY_PIN = "user_pin"
    private const val KEY_LOCKED_APPS = "locked_apps"

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

    fun getLockedApps(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_LOCKED_APPS, emptySet()) ?: emptySet()
    }

    fun setLockedApps(context: Context, apps: Set<String>) {
        getPrefs(context).edit().putStringSet(KEY_LOCKED_APPS, apps).apply()
    }
}
