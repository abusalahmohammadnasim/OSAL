package com.example.applock.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.applock.databinding.ActivityPinSetupBinding
import com.example.applock.util.PrefsHelper

class PinSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPinSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            val pin1 = binding.etPin1.text.toString()
            val pin2 = binding.etPin2.text.toString()
            when {
                pin1.length < 4 -> toast("PIN must be at least 4 digits")
                pin1 != pin2 -> toast("PINs don't match")
                else -> {
                    PrefsHelper.setPin(this, pin1)
                    toast("PIN saved")
                    finish()
                }
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
