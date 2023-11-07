package com.ncs.o2.UI.StartScreen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.databinding.ActivityMaintainingScreenBinding

class maintainingScreen : AppCompatActivity() {

    private lateinit var binding: ActivityMaintainingScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMaintainingScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvMaintaining.text = Codes.STRINGS.maintaninDesc
        binding.tvRetry.setOnClickListener {
            refresh()
        }
        disableBackPress()
    }

    private fun refresh(){
        val intent = Intent(this, StartScreen::class.java)
        startActivity(intent)
    }

    private fun disableBackPress() {
        onBackPressedDispatcher.addCallback(this) {
            refresh()
        }
    }
}