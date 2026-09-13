package com.example.applock.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.R
import com.example.applock.util.PrefsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PrefsHelper.isPinSet(this)) {
            startActivity(Intent(this, PinSetupActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        setupRecyclerView()
        checkPermissionsPrompt()
    }

    override fun onResume() {
        super.onResume()
        loadInstalledApps()
    }

    private fun setupRecyclerView() {
        val rvApps = findViewById<RecyclerView>(R.id.rvApps)
        
        adapter = AppListAdapter(emptyList()) { appInfo, isChecked ->
            val lockedApps = PrefsHelper.getLockedApps(this).toMutableSet()
            if (isChecked) {
                lockedApps.add(appInfo.packageName)
            } else {
                lockedApps.remove(appInfo.packageName)
            }
            PrefsHelper.setLockedApps(this, lockedApps)
            appInfo.isLocked = isChecked
        }
        
        rvApps.layoutManager = LinearLayoutManager(this)
        rvApps.adapter = adapter
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        }

        val lockedApps = PrefsHelper.getLockedApps(this)
        val appList = mutableListOf<AppInfo>()

        for (pkgInfo in packages) {
            val packageName = pkgInfo.packageName

            if (packageName != this.packageName) {
                val intent = pm.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    val appName = pkgInfo.applicationInfo.loadLabel(pm).toString()
                    val icon = pkgInfo.applicationInfo.loadIcon(pm)
                    val isLocked = lockedApps.contains(packageName)

                    appList.add(AppInfo(appName, packageName, icon, isLocked))
                }
            }
        }

        appList.sortBy { it.appName.lowercase() }
        adapter.updateApps(appList)
    }

    private fun checkPermissionsPrompt() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
}
