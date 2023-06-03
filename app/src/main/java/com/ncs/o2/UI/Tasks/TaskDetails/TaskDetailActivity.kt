package com.ncs.o2.UI.Tasks.TaskDetails

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import com.ncs.o2.Services.ApiClient
import com.ncs.o2.Services.NotificationApiService
import com.ncs.o2.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Utility.ExtensionsUtil.gone
import com.ncs.o2.Utility.ExtensionsUtil.setOnClickBounceListener
import com.ncs.o2.Utility.ExtensionsUtil.snackbar
import com.ncs.o2.Utility.ExtensionsUtil.visible
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject


@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity() {

    private val binding: ActivityTaskDetailBinding by lazy {
        ActivityTaskDetailBinding.inflate(layoutInflater)
    }

    private val viewModel: TaskDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        binding.requestButton.setOnClickBounceListener {


            binding.progressBar.visible()
            binding.progressBar.animFadein(this)
            binding.requestText.animFadein(this)
            binding.requestText.text = "Sending request"
            viewModel.sendNotification()



        }




        binding.taskStatus.setOnClickBounceListener {}
        binding.duration.setOnClickBounceListener {}
        binding.difficulty.setOnClickBounceListener {}
        binding.tagLayout.setOnClickBounceListener { }

        binding.gioActionbar.btnBack.setOnClickBounceListener {
            onBackPressed()
        }


    }


}

