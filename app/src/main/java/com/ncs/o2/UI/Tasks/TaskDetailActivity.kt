package com.ncs.o2.UI.Tasks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityTaskDetailBinding

class TaskDetailActivity : AppCompatActivity() {

    private val binding: ActivityTaskDetailBinding by lazy {
        ActivityTaskDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        binding.gioActionbar.btnBack.setOnClickListener{
            onBackPressed()
        }
    }
}