package com.ncs.o2.UI.Tasks.TaskDetails

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity() {
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    val binding: ActivityTaskDetailBinding by lazy {
        ActivityTaskDetailBinding.inflate(layoutInflater)
    }
    lateinit var taskId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        taskId = intent.getStringExtra("task_id")!!
        setActionbar()
        binding.gioActionbar.btnBack.setOnClickThrottleBounceListener {
            onBackPressed()
        }
        binding.gioActionbar.btnFav.setOnClickThrottleBounceListener{
            binding.gioActionbar.btnFav.setImageDrawable(resources.getDrawable(R.drawable.star_filled))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        finish()
    }
    private fun setActionbar() {
        binding.gioActionbar.titleTv.text = taskId
        binding.gioActionbar.doneItem.gone()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment, TasksDetailsHolderFragment())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
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
            Tag("Critical", Colors.WHITE, Colors.BLACK,""),
            Tag("Bug", Colors.RED, Colors.WHITE,""),
            Tag("Feature", Colors.BLUE, Colors.WHITE,""),
            Tag("New", Colors.GREEN, Colors.BLACK,""),
            Tag("Critical", Colors.WHITE, Colors.BLACK,""),

            )
        val adapter = TagAdapter(dataList)
        tagsRecyclerView.adapter = adapter
    }

    override fun onProfileClick(user: User, position: Int) {
        val bottomSheet = user.profileDPUrl?.let { ProfileBottomSheet(it) }
        bottomSheet?.show(supportFragmentManager, "bottomsheet")
    }

    override fun removeClick(user: User, position: Int) {
        TODO("Not yet implemented")
    }



}

