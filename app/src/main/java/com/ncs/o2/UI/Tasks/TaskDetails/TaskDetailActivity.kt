package com.ncs.o2.UI.Tasks.TaskDetails

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.database.collection.LLRBNode
import com.google.gson.JsonObject
import com.ncs.o2.Adapters.TagAdapter
import com.ncs.o2.Models.Tag
import com.ncs.o2.Services.ApiClient
import com.ncs.o2.Services.NotificationApiService
import com.ncs.o2.Utility.Colors
import com.ncs.o2.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Utility.ExtensionsUtil.gone
import com.ncs.o2.Utility.ExtensionsUtil.setOnClickBounceListener
import com.ncs.o2.Utility.ExtensionsUtil.snackbar
import com.ncs.o2.Utility.ExtensionsUtil.visible
import com.ncs.o2.Utility.Later
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

        setUpViews()



    }

    @Later("1. Check if the request has already made, if made then set text and clickability on the button accordingly")
    private fun setUpViews() {


        setTagsView()

        binding.requestButton.setOnClickBounceListener {

            binding.requestText.animFadein(this)
            binding.requestText.text = "Work Request Sent"
            viewModel.sendNotification()
            binding.requestButton.isClickable = false
            binding.requestButton.alpha = 0.7f
        }

        binding.taskStatus.setOnClickBounceListener {}
        binding.duration.setOnClickBounceListener {}
        binding.difficulty.setOnClickBounceListener {}

        binding.gioActionbar.btnBack.setOnClickBounceListener {
            onBackPressed()
        }


    }

    private fun setTagsView() {

        val tagsRecyclerView = binding.tagRecyclerView
        val layoutManager= FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW;
        layoutManager.flexWrap = FlexWrap.WRAP
        tagsRecyclerView.layoutManager = layoutManager

        val dataList = listOf(
            Tag("Critical",  Colors.WHITE, Colors.BLACK),
            Tag("Bug", Colors.RED, Colors.WHITE),
            Tag("Feature", Colors.BLUE, Colors.WHITE),
            Tag("New", Colors.GREEN, Colors.BLACK),
            Tag("Critical",  Colors.WHITE, Colors.BLACK),
            Tag("Bug", Colors.RED, Colors.WHITE),
            Tag("Feature", Colors.BLUE, Colors.WHITE),
            Tag("New", Colors.GREEN, Colors.BLACK),
            Tag("Critical",  Colors.WHITE, Colors.BLACK),
            Tag("Bug", Colors.RED, Colors.WHITE),
            Tag("Feature", Colors.BLUE, Colors.WHITE),
            Tag("New", Colors.GREEN, Colors.BLACK),

        )
        val adapter = TagAdapter(dataList)
        tagsRecyclerView.adapter = adapter
    }


}

