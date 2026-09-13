package com.example.applock.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
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
import com.example.applock.receiver.AdminReceiver
import com.example.applock.util.PrefsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var rvApps: RecyclerView
    private lateinit var adapter: AppListAdapter
    private val ADMIN_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvApps = findViewById(R.id.rvApps)
        rvApps.layoutManager = LinearLayoutManager(this)

        checkAndEnableDeviceAdmin()
        checkAccessibilityPermission()
        loadAllApps()
    }

    private fun checkAndEnableDeviceAdmin() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val compName = ComponentName(this, AdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(compName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable Device Admin to prevent unauthorized uninstallation.")
            }
            startActivityForResult(intent, ADMIN_REQUEST_CODE)
        }
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

        for (app in packages) {
            val appName = pm.getApplicationLabel(app).toString()
            val packageName = app.packageName
            val icon = pm.getApplicationIcon(app)

            if (packageName != this.packageName) {
                val isLocked = PrefsHelper.isAppLocked(this, packageName)
                appList.add(AppInfo(appName, packageName, icon, isLocked))
            }
        }

        appList.sortBy { it.appName.lowercase() }
        adapter = AppListAdapter(appList) { appInfo, isLocked ->
            PrefsHelper.setAppLocked(this, appInfo.packageName, isLocked)
        }
        rvApps.adapter = adapter
    }
}
