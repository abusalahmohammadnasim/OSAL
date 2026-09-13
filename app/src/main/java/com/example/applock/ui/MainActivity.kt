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
import com.example.applock.databinding.ActivityMainBinding
import com.example.applock.util.PrefsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PrefsHelper.isPinSet(this)) {
            startActivity(Intent(this, PinSetupActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        checkPermissionsPrompt()
    }

    override fun onResume() {
        super.onResume()
        loadInstalledApps()
    }

    private fun setupRecyclerView() {
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
        
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter
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

            // নিজ অ্যাপ বাদে ইনস্টল করা ইউজার ও সিস্টেমের সমস্ত লঞ্চ করা সম্ভব এমন অ্যাপ ফিল্টার করে আনা হচ্ছে
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
        // ১. ওভারলে (Draw Overlays) পারমিশন না থাকলে চাওয়া হবে
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        // ২. ব্যাটারি অপটিমাইজেশন বন্ধ রাখার পারমিশন চাওয়া হবে যাতে ব্যাকগ্রাউন্ড সার্ভিস নিজে থেকে বন্ধ না হয়
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
}
