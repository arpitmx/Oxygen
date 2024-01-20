package com.ncs.o2.UI.Teams

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityMainBinding
import com.ncs.o2.databinding.ActivityTeamsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamsActivity : AppCompatActivity() {

    val binding: ActivityTeamsBinding by lazy {
        ActivityTeamsBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}