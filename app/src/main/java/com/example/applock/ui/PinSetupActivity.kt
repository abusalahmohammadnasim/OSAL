package com.example.applock.ui

import android.content.Intent
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

        binding.btnSavePin.setOnClickListener {
            val pin = binding.etPin.text.toString().trim()
            if (pin.length >= 4) {
                // এখানে savePin এর পরিবর্তে আসল মেথড setPin ব্যবহার করা হয়েছে
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
