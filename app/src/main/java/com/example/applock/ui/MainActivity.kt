package com.example.applock.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applock.databinding.ActivityMainBinding
import com.example.applock.util.PrefsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate()

        // First-run: redirect to PIN setup if no PIN is saved yet
        if (PrefsHelper.getPin(this) == null) {
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
        adapter = AppListAdapter { appInfo, isLocked ->
            PrefsHelper.setAppLocked(this, appInfo.packageName, isLocked)
        }
        binding.recyclerViewApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewApps.adapter = adapter
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        
        // GET_META_DATA দিয়ে সিস্টেম অ্যাপসহ সব প্যাকেজ আনা হয়
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        }

        val appList = mutableListOf<AppInfo>()

        for (pkgInfo in packages) {
            val packageName = pkgInfo.packageName

            // নিজের অ্যাপ লকার ব্যতীত প্যাকেজ ইনস্টলার সহ সব অ্যাপ তালিকায় যুক্ত হবে
            if (packageName != this.packageName) {
                val appName = pkgInfo.applicationInfo.loadLabel(pm).toString()
                val icon = pkgInfo.applicationInfo.loadIcon(pm)
                val isLocked = PrefsHelper.isAppLocked(this, packageName)

                appList.add(AppInfo(appName, packageName, icon, isLocked))
            }
        }

        // অ্যাপের নাম অনুযায়ী সাজানো
        appList.sortBy { it.name.lowercase() }
        adapter.submitList(appList)
    }

    private fun checkPermissionsPrompt() {
        // Prompt for Overlay permission if not granted
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }
}
