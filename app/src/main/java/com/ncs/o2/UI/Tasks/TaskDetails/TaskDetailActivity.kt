package com.ncs.o2.UI.Tasks.TaskDetails

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.ncs.o2.UI.UIComponents.Adapters.TagAdapter
import com.ncs.o2.UI.UIComponents.BottomSheets.ProfileBottomSheet
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Utility.Colors
import com.ncs.o2.Domain.Utility.ExtensionsUtil.animFadein
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.R
import com.ncs.o2.UI.UIComponents.Adapters.ContributorAdapter
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity(), ContributorAdapter.OnProfileClickCallback {

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
        setActionbar()

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
            onBackPressedDispatcher.onBackPressed()
        }


    }

    private fun setActionbar() {
        binding.gioActionbar.titleTv.text = getString(R.string.details)
        binding.gioActionbar.doneItem.gone()
    }

    private fun setContributors() {

        val contriRecyclerView = binding.contributorsRecyclerView
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        contriRecyclerView.layoutManager = layoutManager


        val dataList = listOf(
            User("https://yt3.googleusercontent.com/xIPexCvioEFPIq_nuEOOsv129614S3K-AblTK2P1L9GvVIZ6wmhz7VyCT-aENMZfCzXU-qUpaA=s900-c-k-c0x00ffffff-no-rj"),
            User("https://hips.hearstapps.com/hmg-prod/images/apple-ceo-steve-jobs-speaks-during-an-apple-special-event-news-photo-1683661736.jpg?crop=0.800xw:0.563xh;0.0657xw,0.0147xh&resize=1200:*"),
            User("https://picsum.photos/200"),
            User("https://picsum.photos/300"),
            User("https://picsum.photos/350"),
            User("https://picsum.photos/450"),
            User("https://picsum.photos/230"),
            User("https://picsum.photos/231"),
            User("https://picsum.photos/202"),
            User("https://picsum.photos/234")
        )

        val adapter = ContributorAdapter(dataList as MutableList<User>, this,false)
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

    override fun onProfileClick(user: User, position: Int) {
        val bottomSheet = ProfileBottomSheet(user.profileDPUrl)
        bottomSheet.show(supportFragmentManager, "bottomsheet")
    }

    override fun removeClick(user: User, position: Int) {
        TODO("Not yet implemented")
    }


}

