package com.example.applock.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.R

class MainActivity : AppCompatActivity() {

    private lateinit var rvApps: RecyclerView
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("app_pin", "")

        if (savedPin.isNullOrEmpty()) {
            val pinIntent = Intent(this, PinSetupActivity::class.java)
            startActivity(pinIntent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        rvApps = findViewById(R.id.rvApps)
        rvApps.layoutManager = LinearLayoutManager(this)

        checkAccessibilityPermission()
        loadAllApps()
    }

    private fun checkAccessibilityPermission() {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )
        if (accessibilityEnabled == 0) {
            Toast.makeText(this, "Please enable Accessibility Service for AppLock", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    private fun loadAllApps() {
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appList = mutableListOf<AppInfo>()
        val prefs = getSharedPreferences("app_lock_prefs", Context.MODE_PRIVATE)

        for (app in packages) {
            val appName = pm.getApplicationLabel(app).toString()
            val packageName = app.packageName
            val icon = pm.getApplicationIcon(app)

            if (packageName != this.packageName) {
                val isLocked = prefs.getBoolean(packageName, false)
                appList.add(AppInfo(appName, packageName, icon, isLocked))
            }
        }

        appList.sortBy { item -> item.appName.lowercase() }
        adapter = AppListAdapter(appList) { appInfo: AppInfo, isLocked: Boolean ->
            prefs.edit().putBoolean(appInfo.packageName, isLocked).apply()
        }
        rvApps.adapter = adapter
    }
}
