package com.ncs.o2.UI.Tasks.TaskPage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.R
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.databinding.ActivityTaskDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class TaskDetailActivity : AppCompatActivity(), TaskDetailsFragment.ViewVisibilityListner {
    @Inject
    lateinit var utils : GlobalUtils.EasyElements
    public val binding: ActivityTaskDetailBinding by lazy {
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
    }

    override fun showProgressbar(show: Boolean) {
        if (show) binding.progressbarBottomTab.visible()
        else binding.progressbarBottomTab.gone()
    }

}
