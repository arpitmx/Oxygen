package com.ncs.o2.UI.Auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.ChooserScreen.ChooserFragment
import com.ncs.o2.databinding.ActivityAuthScreenBinding

class AuthScreenActivity : AppCompatActivity() {

    private val binding: ActivityAuthScreenBinding by lazy {
        ActivityAuthScreenBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpViews()


    }

    private fun setUpViews() {

    }
}