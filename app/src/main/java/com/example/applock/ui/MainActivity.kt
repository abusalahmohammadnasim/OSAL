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

        // PIN না দেওয়া থাকলে সেটিংস পেজে পাঠাবে
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
        // AppListAdapter সঠিক ২টি প্যারামিটার নিচ্ছে
        adapter = AppListAdapter(this) { appInfo ->
            val updatedState = !appInfo.isLocked
            PrefsHelper.setAppLocked(this, appInfo.packageName, updatedState)
            loadInstalledApps()
        }
        
        // ViewBinding এ recyclerViewApps ব্যবহার
        binding.recyclerViewApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewApps.adapter = adapter
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

            // নিজ অ্যাপ ছাড়া প্যাকেজ ইনস্টলার সহ সব অ্যাপ ফিল্টার করবে
            if (packageName != this.packageName) {
                val name = pkgInfo.applicationInfo.loadLabel(pm).toString()
                val icon = pkgInfo.applicationInfo.loadIcon(pm)
                val isLocked = PrefsHelper.isAppLocked(this, packageName)

                // AppInfo মডেলে সঠিক ফিল্ড নাম `name`
                appList.add(AppInfo(name, packageName, icon, isLocked))
            }
        }

        appList.sortBy { it.name.lowercase() }
        adapter.submitList(appList)
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
