package com.ncs.o2.UI.Tasks.TaskDetails

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ncs.o2.Adapters.ContributorAdapter
import com.ncs.o2.Adapters.TagAdapter
import com.ncs.o2.BottomSheets.ProfileBottomSheet
import com.ncs.o2.Models.Contributor
import com.ncs.o2.Models.Tag
import com.ncs.o2.Utility.Colors
import com.ncs.o2.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Utility.Later
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity(), ContributorAdapter.OnClickCallback {

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
        setContributors()

        binding.requestButton.setOnClickSingleTimeBounceListener {

            binding.requestText.animFadein(this)
            binding.requestText.text = "Work Request Sent"
            viewModel.sendNotification()
            binding.requestButton.isClickable = false
            binding.requestButton.alpha = 0.7f
        }

        binding.taskStatus.setOnClickThrottleBounceListener {}
        binding.duration.setOnClickThrottleBounceListener {}
        binding.difficulty.setOnClickThrottleBounceListener {}

        binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
            onBackPressed()
        }


    }

    private fun setContributors() {

        val contriRecyclerView = binding.contributorsRecyclerView
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        contriRecyclerView.layoutManager = layoutManager


        val dataList = listOf(
            Contributor("https://yt3.googleusercontent.com/xIPexCvioEFPIq_nuEOOsv129614S3K-AblTK2P1L9GvVIZ6wmhz7VyCT-aENMZfCzXU-qUpaA=s900-c-k-c0x00ffffff-no-rj"),
            Contributor("https://hips.hearstapps.com/hmg-prod/images/apple-ceo-steve-jobs-speaks-during-an-apple-special-event-news-photo-1683661736.jpg?crop=0.800xw:0.563xh;0.0657xw,0.0147xh&resize=1200:*"),
            Contributor("https://picsum.photos/200"),
            Contributor("https://picsum.photos/300"),
            Contributor("https://picsum.photos/350"),
            Contributor("https://picsum.photos/450"),
            Contributor("https://picsum.photos/230"),
            Contributor("https://picsum.photos/231"),
            Contributor("https://picsum.photos/202"),
            Contributor("https://picsum.photos/234")
        )
        val adapter = ContributorAdapter(dataList, this)
        contriRecyclerView.adapter = adapter
    }

    private fun setTagsView() {

        val tagsRecyclerView = binding.tagRecyclerView
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        tagsRecyclerView.layoutManager = layoutManager

        val dataList = listOf(
            Tag("Critical", Colors.WHITE, Colors.BLACK),
            Tag("Bug", Colors.RED, Colors.WHITE),
            Tag("Feature", Colors.BLUE, Colors.WHITE),
            Tag("New", Colors.GREEN, Colors.BLACK),
            Tag("Critical", Colors.WHITE, Colors.BLACK),

            )
        val adapter = TagAdapter(dataList)
        tagsRecyclerView.adapter = adapter
    }

    override fun onClick(contributor: Contributor, position: Int) {
        val bottomSheet = ProfileBottomSheet(contributor.profileUrl)
        bottomSheet.show(supportFragmentManager, "bottomsheet")
    }


}

