package com.example.applock.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.applock.databinding.ActivityLockScreenBinding
import com.example.applock.service.AppLockAccessibilityService
import com.example.applock.util.BiometricHelper
import com.example.applock.util.PrefsHelper

class LockScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockScreenBinding
    private var targetPackage: String = ""
    private var verifyOnly: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetPackage = intent.getStringExtra(EXTRA_TARGET_PACKAGE) ?: ""
        verifyOnly = intent.getBooleanExtra(EXTRA_VERIFY_ONLY, false)
        binding.tvAppName.text = if (verifyOnly) {
            "Enter PIN to open\nApp Lock Settings"
        } else {
            "Enter PIN to open\n${appLabel(targetPackage)}"
        }

        binding.btnUnlock.setOnClickListener { attemptUnlock() }
        binding.etPin.setOnEditorActionListener { _, _, _ -> attemptUnlock(); true }

        val biometricReady = PrefsHelper.isBiometricEnabled(this) && BiometricHelper.isAvailable(this)
        if (biometricReady) {
            binding.btnFingerprint.visibility = android.view.View.VISIBLE
            binding.btnFingerprint.setOnClickListener { showBiometricPrompt() }
            showBiometricPrompt() // prompt immediately on screen open
        }
    }

    private fun showBiometricPrompt() {
        BiometricHelper.prompt(
            activity = this,
            title = if (verifyOnly) "Unlock App Lock Settings" else "Unlock ${appLabel(targetPackage)}",
            onSuccess = { onUnlockSuccess() },
            onFailure = { /* user cancelled or hit "Use PIN instead" -> just let them type the PIN */ }
        )
    }

    private fun attemptUnlock() {
        val pin = binding.etPin.text.toString()
        if (PrefsHelper.verifyPin(this, pin)) {
            onUnlockSuccess()
        } else {
            binding.tvError.visibility = android.view.View.VISIBLE
            binding.etPin.text.clear()
        }
    }

    private fun onUnlockSuccess() {
        if (verifyOnly) {
            setResult(RESULT_OK)
        } else {
            AppLockAccessibilityService.instance?.markUnlocked(targetPackage)
        }
        finish()
    }

    private fun appLabel(pkg: String): String {
        return try {
            val pm = packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            pkg
        }
    }

    // Pressing back should not reveal the locked app / settings screen.
    override fun onBackPressed() {
        if (verifyOnly) {
            setResult(RESULT_CANCELED)
            finish()
        } else {
            moveTaskToBack(true)
        }
    }

    companion object {
        const val EXTRA_TARGET_PACKAGE = "target_package"
        // When true, this screen is gating access to AppLock's own settings
        // UI rather than gating a third-party app.
        const val EXTRA_VERIFY_ONLY = "verify_only"
    }
}
