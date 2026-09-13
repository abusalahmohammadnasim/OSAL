package com.example.applock.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.applock.R
import com.example.applock.receiver.MyDeviceAdminReceiver

class MainActivity : AppCompatActivity() {

    private lateinit var rvApps: RecyclerView
    private lateinit var adapter: AppListAdapter
    private val ADMIN_REQUEST_CODE = 123

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

        checkAndEnableDeviceAdmin()
        checkAccessibilityPermission()
        loadAllApps()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (::adapter.isInitialized) {
                    adapter.filter(newText ?: "")
                }
                return true
            }
        })
        return true
    }

    private fun checkAndEnableDeviceAdmin() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val compName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(compName)) {
            val adminIntent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            adminIntent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            adminIntent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Enable Device Admin to prevent unauthorized uninstallation.")
            startActivityForResult(adminIntent, ADMIN_REQUEST_CODE)
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

        appList.sortBy { it.appName.lowercase() }
        adapter = AppListAdapter(appList) { appInfo, isLocked ->
            prefs.edit().putBoolean(appInfo.packageName, isLocked).apply()
        }
        rvApps.adapter = adapter
    }
}
