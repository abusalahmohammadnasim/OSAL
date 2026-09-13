package com.example.applock.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applock.R
import com.example.applock.util.PrefsHelper

class LockScreenActivity : AppCompatActivity() {

    private lateinit var etPin: EditText
    private lateinit var btnUnlock: Button
    private lateinit var tvStatus: TextView
    private var targetPackage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        targetPackage = intent.getStringExtra("TARGET_PACKAGE")

        etPin = findViewById(R.id.etPin)
        btnUnlock = findViewById(R.id.btnUnlock)
        tvStatus = findViewById(R.id.tvStatus)

        btnUnlock.setOnClickListener {
            val enteredPin = etPin.text.toString().trim()
            if (enteredPin.isEmpty()) {
                Toast.makeText(this, "Enter PIN", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (PrefsHelper.verifyPin(this, enteredPin)) {
                finish()
            } else {
                tvStatus.text = "Incorrect PIN! Try again."
                etPin.text.clear()
            }
        }
    }

    override fun onBackPressed() {
        // Go to Home Screen instead of unlocking app on back press
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
