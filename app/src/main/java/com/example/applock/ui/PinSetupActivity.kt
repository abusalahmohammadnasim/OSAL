package com.example.applock.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applock.R
import com.example.applock.util.PrefsHelper

class PinSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_setup)

        val btnSavePin = findViewById<Button>(R.id.btnSavePin)
        val etPin = findViewById<EditText>(R.id.etPin)

        btnSavePin.setOnClickListener {
            val pin = etPin.text.toString().trim()
            if (pin.length >= 4) {
                PrefsHelper.setPin(this, pin)
                Toast.makeText(this, "PIN Saved Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Enter at least 4 digits PIN", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
