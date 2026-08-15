package com.example.applock.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.applock.databinding.ActivityMainBinding
import com.example.applock.service.MyDeviceAdminReceiver
import com.example.applock.util.BiometricHelper
import com.example.applock.util.PrefsHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val verifyLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            initUi()
        } else {
            // Wrong PIN dismissed or back-pressed out of the PIN screen:
            // do not reveal settings, just close the app.
            finishAffinity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!PrefsHelper.isPinSet(this)) {
            // First run: no PIN exists yet, nothing to gate against.
            initUi()
            startActivity(Intent(this, PinSetupActivity::class.java))
            return
        }

        // A PIN already exists: require it before showing anything,
        // so the lock list can't be opened and edited by anyone who
        // just taps the AppLock icon.
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            putExtra(LockScreenActivity.EXTRA_VERIFY_ONLY, true)
        }
        verifyLauncher.launch(intent)
    }

    private fun initUi() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSetPin.setOnClickListener {
            startActivity(Intent(this, PinSetupActivity::class.java))
        }

        binding.btnEnableAccessibility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.btnEnableAdmin.setOnClickListener {
            val compName = ComponentName(this, MyDeviceAdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Enabling this stops App Lock from being uninstalled with a simple tap."
                )
            }
            startActivity(intent)
        }

        binding.btnEnableOverlay.setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        val biometricAvailability = BiometricHelper.checkAvailability(this)
        val biometricSupported = biometricAvailability == BiometricHelper.Availability.READY
        binding.switchBiometric.isEnabled = biometricSupported
        binding.switchBiometric.isChecked = biometricSupported && PrefsHelper.isBiometricEnabled(this)
        binding.tvBiometricLabel.text = when (biometricAvailability) {
            BiometricHelper.Availability.READY -> "Use fingerprint / face unlock"
            BiometricHelper.Availability.NONE_ENROLLED -> "No fingerprint enrolled — tap to fix"
            BiometricHelper.Availability.NO_HARDWARE -> "No fingerprint sensor on this device"
            BiometricHelper.Availability.HARDWARE_UNAVAILABLE -> "Fingerprint sensor busy — try again"
            BiometricHelper.Availability.UNKNOWN -> "Not available on this device"
        }
        binding.switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            PrefsHelper.setBiometricEnabled(this, isChecked)
        }
        if (biometricAvailability == BiometricHelper.Availability.NONE_ENROLLED) {
            binding.rowBiometric.setOnClickListener {
                val intent = if (android.os.Build.VERSION.SDK_INT >= 30) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                } else {
                    Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                }
                startActivity(intent)
            }
        }

        setupAppList()
    }

    private fun setupAppList() {
        val pm = packageManager
        val lockedApps = PrefsHelper.getLockedApps(this)

        val apps = pm.getInstalledApplications(0)
            .filter { it.packageName != packageName } // don't allow locking ourselves
            .filter {
                // show user-facing apps: either non-system, or system apps that have a launch intent
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || pm.getLaunchIntentForPackage(it.packageName) != null
            }
            .map {
                AppInfo(
                    packageName = it.packageName,
                    label = pm.getApplicationLabel(it).toString(),
                    icon = pm.getApplicationIcon(it),
                    locked = lockedApps.contains(it.packageName)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toMutableList()

        val adapter = AppListAdapter(apps) { _, _ ->
            val newLocked = apps.filter { it.locked }.map { it.packageName }.toSet()
            PrefsHelper.setLockedApps(this, newLocked)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
}
