package com.example.applock.ui

import android.content.Intent
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
        super.onCreate(savedInstanceState)

        // Redirect to PIN setup if PIN is not configured yet
        if (!PrefsHelper.hasPin(this)) {
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
        adapter = AppListAdapter(emptyList()) { appInfo, isLocked ->
            PrefsHelper.setLocked(this, appInfo.packageName, isLocked)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        }

        val appList = mutableListOf<AppInfo>()

        for (pkgInfo in packages) {
            val packageName = pkgInfo.packageName

            // Includes package installer and all system apps except this applock itself
            if (packageName != this.packageName) {
                val appName = pkgInfo.applicationInfo.loadLabel(pm).toString()
                val icon = pkgInfo.applicationInfo.loadIcon(pm)
                val isLocked = PrefsHelper.isLocked(this, packageName)

                appList.add(AppInfo(appName, packageName, icon, isLocked))
            }
        }

        appList.sortBy { it.appName.lowercase() }
        adapter.updateApps(appList)
    }

    private fun checkPermissionsPrompt() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }
}
